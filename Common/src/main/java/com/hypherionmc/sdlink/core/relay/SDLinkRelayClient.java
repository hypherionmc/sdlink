package com.hypherionmc.sdlink.core.relay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.craterlib.api.game.world.level.CraterCommonGameRules;
import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.SDLinkRelayConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.neovisionaries.ws.client.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SDLinkRelayClient extends WebSocketAdapter {

    public static final SDLinkRelayClient INSTANCE = new SDLinkRelayClient();

    // Internal Values
    private WebSocket webSocket;
    private EncryptionUtil encryption;
    private final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final Logger logger = LoggerFactory.getLogger(SDLinkRelayClient.class);

    // Retry Handler
    private int retryCount = 0;
    private boolean wasClose = false;

    @Getter
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Open, or try to open a new connection to a Relay Server
     */
    public void openConnection() {
        // Relay server is disabled, so we shut down the server
        if (!SDLinkRelayConfig.INSTANCE.relayServer.enabled) {
            closeServer(true);
            return;
        }

        // Reconnect worker is shutdown, so we start a new one
        if (scheduler.isShutdown())
            scheduler = Executors.newSingleThreadScheduledExecutor();

        // Get the unique token from the config, and check that it's not empty
        String identifier = EncryptionUtil.INSTANCE.decrypt(SDLinkRelayConfig.INSTANCE.relayServer.relayToken);

        if (identifier == null || identifier.isEmpty()) {
            BotController.INSTANCE.getLogger().error("Relay Server Token cannot be empty!");
            return;
        }

        try {
            // Close the server, just in case it's open
            closeServer(false);

            logger.info("Connecting to Relay Server at {}", SDLinkRelayConfig.INSTANCE.relayServer.relayServerUrl);
            encryption = new EncryptionUtil(identifier);

            webSocket = new WebSocketFactory().createSocket(String.format("wss://%s?identifier=%s&serverName=%s", SDLinkRelayConfig.INSTANCE.relayServer.relayServerUrl, identifier, URLEncoder.encode(SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName, StandardCharsets.UTF_8)));
            webSocket.setPingInterval(10000); // Keep the WebSocket alive
            webSocket.addListener(this);
            webSocket.connectAsynchronously();
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to open connection to Relay Server", e);
            scheduleReconnect();
        }
    }

    /**
     * Broadcast a message to all connected Relay Clients, with the same token
     *
     * @param message The {@link RelayMessage} instance to process
     */
    public void relayMessage(RelayMessage message) {
        if (webSocket == null || !webSocket.isOpen())
            return;

        try {
            String json = encryption.encrypt(GSON.toJson(message));
            webSocket.sendText(json);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to send relay message", e);
        }
    }

    /**
     * Shut down the relay server.
     *
     * @param wasClose Was this close triggered by a config reload. Determines if the connection should be retried
     */
    public void closeServer(boolean wasClose) {
        if (wasClose) {
            scheduler.shutdownNow();
            this.wasClose = true;
        }

        try {
            if (webSocket != null && webSocket.isOpen()) {
                webSocket.disconnect();
                logger.info("Disconnected from Relay Server");
            }
        } catch (Exception ignored) {}
    }

    // We are ready for business. The connection is open
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        logger.info("Successfully connected to Relay Server at {}", SDLinkRelayConfig.INSTANCE.relayServer.relayServerUrl);
        retryCount = 0;
    }

    // We got disconnected.... Oh boy
    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        if (closedByServer) {
            logger.error("Relay Server Connection closed by Server");
        } else {
            if (wasClose)
                return;

            logger.error("Disconnected from Relay Server. Attempting to reconnect...");
            scheduleReconnect();
        }
    }

    // This is fine....
    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        logger.error("Relay WebSocket Error: {}", cause.getMessage());
    }

    /**
     * Retry to connect to the WebSocket server, if it disconnects
     */
    private void scheduleReconnect() {
        int maxRetries = 10;
        if (retryCount >= maxRetries) {
            logger.error("Relay Server Max Reconnect retries reached. Giving up....");
            return;
        }

        long INITIAL_BACKOFF_MS = 1000;
        long MAX_BACKOFF_MS = 300000;
        long delay = Math.min(INITIAL_BACKOFF_MS * (1L << retryCount), MAX_BACKOFF_MS);
        retryCount++;

        logger.warn("Reconnecting in {} seconds...", (delay / 1000));
        scheduler.schedule(this::openConnection, delay, TimeUnit.MILLISECONDS);
    }

    // Message received from the relay server
    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String payload = frame.getPayloadText();

        try {
            RelayMessage relayMessage = GSON.fromJson(encryption.decrypt(payload), RelayMessage.class);
            handleMessage(relayMessage);
        } catch (Exception e) {
            logger.error("Failed to process incoming relay message", e);
        }
    }

    /**
     * Handle a message received from the Relay Server
     *
     * @param relayMessage The decoded {@link RelayMessage} we received from the server
     */
    private void handleMessage(RelayMessage relayMessage) {
        // Check that the required data is present
        if ((relayMessage.getData() == null && relayMessage.getMessage() == null)
                || relayMessage.getServerName() == null || relayMessage.getServerName().isEmpty()) return;

        // Set up the prefix
        String prefix = SDLinkRelayConfig.INSTANCE.messageConfig.relayMessagePrefix.replace("%server_name%", relayMessage.getServerName()) + " ";

        // Set up the prefix component for Minecraft
        Text base = prefix.trim().isEmpty() ? Text.empty() : Text.formatted(prefix);

        // Message was a user message sent in discord
        if (relayMessage.getType() == RelayMessage.MessageType.DISCORD) {
            ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                    base.append(Text.fromJson(relayMessage.getMessage())),
                    false
            );
            return;
        }

        // Get the data packet
        DataMessage dataMessage = relayMessage.getData();

        switch (relayMessage.getType()) {
            case ADVANCEMENT ->  {
                // Respect the game rules!
                if (!ServerEvents.getInstance().getMinecraftServer().getGameRules().getBoolean(CraterCommonGameRules.RULE_ANNOUNCE_ADVANCEMENTS))
                    return;

                ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                        base.append(Text.translatable(
                                "chat.type.advancement.task",
                                dataMessage.displayName(),
                                dataMessage.additional())),
                        false
                );
            }

            case CHAT -> {
                ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                        base.append(Text.translatable("chat.type.text", dataMessage.displayName(), dataMessage.message())),
                        false
                );
            }

            case DEATH -> {
                if (!ServerEvents.getInstance().getMinecraftServer().getGameRules().getBoolean(CraterCommonGameRules.RULE_SHOWDEATHMESSAGES))
                    return;

                ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                        base.append(dataMessage.message()),
                        false
                );
            }

            case JOIN -> {
                ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                        base.append(Text.translatable("multiplayer.player.joined", dataMessage.displayName())),
                        false
                );
            }

            case LEAVE -> {
                ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                        base.append(Text.translatable("multiplayer.player.left", dataMessage.displayName())),
                        false
                );
            }
        }

        if (SDLinkRelayConfig.INSTANCE.messageConfig.relayMinecraftToDiscord) {
            switch (relayMessage.getType()) {
                case CHAT -> {
                    if (!SDLinkConfig.INSTANCE.chatConfig.playerMessages)
                        return;

                    String username = dataMessage.displayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                    String msg = dataMessage.message().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

                    if (!SDLinkRelayConfig.INSTANCE.messageConfig.relayMessagePrefix.isEmpty()) {
                        username = Text.formatted(prefix + " " + username).asString();
                    }

                    if (SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
                        msg = SDLinkChatUtils.parse(msg);
                    }

                    DiscordAuthor author = DiscordAuthor.of(username, dataMessage.getUuid().toString(), dataMessage.getUsername())
                            .setPlayerName(dataMessage.getUsername());

                    DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                            .message(msg)
                            .embedColor(SDLinkConfig.INSTANCE.messageFormatting.chatColor)
                            .author(!dataMessage.isFromServer() ? author : DiscordAuthor.getServer())
                            .build();

                    discordMessage.sendMessage();
                }

                case JOIN -> {
                    if (!SDLinkConfig.INSTANCE.chatConfig.playerJoin)
                        return;

                    String msg = SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", dataMessage.displayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting));

                    if (!SDLinkRelayConfig.INSTANCE.messageConfig.relayMessagePrefix.isEmpty()) {
                        msg = Text.formatted(prefix + " " + msg).asString();
                    }

                    DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.JOIN)
                            .message(msg)
                            .embedColor(SDLinkConfig.INSTANCE.messageFormatting.playerJoinedColor)
                            .author(DiscordAuthor.getServer()
                                    .setPlayerName(dataMessage.displayName().asString())
                                    .setPlayerAvatar(dataMessage.getUsername(), dataMessage.getUuid().toString()))
                            .build();

                    discordMessage.sendMessage();
                }
                case LEAVE -> {
                    if (!SDLinkConfig.INSTANCE.chatConfig.playerLeave)
                        return;

                    String msg = SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", dataMessage.displayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting));

                    if (!SDLinkRelayConfig.INSTANCE.messageConfig.relayMessagePrefix.isEmpty()) {
                        msg = Text.formatted(prefix + " " + msg).asString();
                    }

                    DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.LEAVE)
                            .message(msg)
                            .embedColor(SDLinkConfig.INSTANCE.messageFormatting.playerLeftColor)
                            .author(DiscordAuthor.getServer()
                                    .setPlayerName(dataMessage.displayName().asString())
                                    .setPlayerAvatar(dataMessage.getUsername(), dataMessage.getUuid().toString()))
                            .build();

                    discordMessage.sendMessage();
                }
                case DEATH -> {
                    String name = dataMessage.displayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                    String msg = dataMessage.message().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                    String finalMessage = SDLinkConfig.INSTANCE.messageFormatting.death;

                    if (msg.startsWith(name + " ")) {
                        msg = msg.substring((name + " ").length());
                    }

                    if (SDLinkConfig.INSTANCE.chatConfig.deathMessages.isFalse()) {
                        return;
                    }

                    finalMessage = finalMessage.replace("%player%", name).replace("%message%", msg);

                    if (!SDLinkRelayConfig.INSTANCE.messageConfig.relayMessagePrefix.isEmpty()) {
                        finalMessage = Text.formatted(prefix + " " + finalMessage).asString();
                    }

                    DiscordMessage message = new DiscordMessageBuilder(MessageType.DEATH)
                            .message(finalMessage)
                            .embedColor(SDLinkConfig.INSTANCE.messageFormatting.deathColor)
                            .author(DiscordAuthor.getServer()
                                    .setPlayerName(dataMessage.displayName().asString())
                                    .setPlayerAvatar(dataMessage.getUsername(), dataMessage.getUuid().toString()))
                            .build();

                    message.sendMessage();
                }
                case ADVANCEMENT -> {
                    String username = dataMessage.displayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                    String finalAdvancement = dataMessage.message().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                    String advancementBody = dataMessage.additional().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

                    String msg = SDLinkConfig.INSTANCE.messageFormatting.achievements.replace("%player%", username).replace("%title%", finalAdvancement).replace("%description%", advancementBody);

                    if (!SDLinkRelayConfig.INSTANCE.messageConfig.relayMessagePrefix.isEmpty()) {
                        msg = Text.formatted(prefix + " " + msg).asString();
                    }

                    DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.ADVANCEMENTS)
                            .message(msg)
                            .embedColor(SDLinkConfig.INSTANCE.messageFormatting.achievementsColor)
                            .author(DiscordAuthor.getServer()
                                    .setPlayerName(dataMessage.displayName().asString())
                                    .setPlayerAvatar(dataMessage.getUsername(), dataMessage.getUuid().toString()))
                            .build();

                    discordMessage.sendMessage();
                }
            }
        }
    }
}
