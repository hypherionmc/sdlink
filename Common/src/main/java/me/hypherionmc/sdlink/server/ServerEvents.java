package me.hypherionmc.sdlink.server;

import com.google.common.collect.Lists;
import com.hypherionmc.craterlib.api.event.common.CraterLivingDeathEvent;
import com.hypherionmc.craterlib.api.event.server.*;
import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.mcdiscordformatter.discord.DiscordSerializer;
import me.hypherionmc.mcdiscordformatter.minecraft.MinecraftSerializer;
import me.hypherionmc.sdlink.SDLinkConstants;
import me.hypherionmc.sdlink.platform.services.ModHelper;
import me.hypherionmc.sdlink.server.commands.DiscordCommand;
import me.hypherionmc.sdlink.server.commands.ReloadModCommand;
import me.hypherionmc.sdlink.server.commands.WhoisCommand;
import me.hypherionmc.sdlink.util.ModUtils;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.discord.DiscordMessage;
import me.hypherionmc.sdlinklib.discord.messages.MessageAuthor;
import me.hypherionmc.sdlinklib.discord.messages.MessageType;
import me.hypherionmc.sdlinklib.events.SDLinkReadyEvent;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.LogReader;
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerEvents implements IMinecraftHelper {

    private final BotController botEngine;
    private MinecraftServer server;
    private final long uptime = System.currentTimeMillis();

    private static ServerEvents events;

    public static ServerEvents getInstance() {
        if (events == null) {
            events = new ServerEvents();
        }
        return events;
    }

    public static void reloadInstance(MinecraftServer server) {
        if (events != null) {
            events.botEngine.shutdownBot(false);
        }
        events = new ServerEvents();
        events.server = server;
    }

    private ServerEvents() {
        botEngine = new BotController(this, SDLinkConstants.LOG);
        botEngine.initializeBot();
    }

    // Modloader Events
    @CraterEventListener
    public void onCommandRegister(CraterRegisterCommandEvent event) {
        DiscordCommand.register(event.getDispatcher());
        WhoisCommand.register(event.getDispatcher());
        ReloadModCommand.register(event.getDispatcher());
    }

    @CraterEventListener
    public void onServerStarting(CraterServerLifecycleEvent.Starting event) {
        this.server = event.getServer();
        if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
            if (ModConfig.INSTANCE.chatConfig.serverStarting) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(ModConfig.INSTANCE.messageConfig.serverStarting)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void onServerStarted(CraterServerLifecycleEvent.Started event) {
        if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
            botEngine.checkWhitelisting();
            if (ModConfig.INSTANCE.chatConfig.serverStarted) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(ModConfig.INSTANCE.messageConfig.serverStarted)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void onServerStopping(CraterServerLifecycleEvent.Stopping event) {
        if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
            if (ModConfig.INSTANCE.chatConfig.serverStopping) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(ModConfig.INSTANCE.messageConfig.serverStopping)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void onServerStoppedEvent(CraterServerLifecycleEvent.Stopped event) {
        if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
            if (ModConfig.INSTANCE.chatConfig.serverStopped) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(ModConfig.INSTANCE.messageConfig.serverStopped)
                .withAuthor(MessageAuthor.SERVER)
                .runAfterSend(botEngine::shutdownBot)
                .build();

                message.sendMessage();
            }
        }
    }

    public void onServerChatEvent(Component message, Component user, String uuid, boolean fromServer) {
        onServerChatEvent(message, user, uuid, null, false);
    }

    @CraterEventListener
    public void onServerChatEvent(CraterServerChatEvent event) {
        onServerChatEvent(event.getComponent(), event.getPlayer().getDisplayName(), ModHelper.INSTANCE.getPlayerSkinUUID(event.getPlayer()), event.getPlayer().getGameProfile(), false);
    }

    public void onServerChatEvent(Component message, Component user, String uuid, GameProfile profile, boolean fromServer) {
        try {
            if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
                if (ModConfig.INSTANCE.chatConfig.playerMessages) {
                    String username = ChatFormatting.stripFormatting(user.getString());
                    String msg = ChatFormatting.stripFormatting(message.getString());

                    if (ModConfig.INSTANCE.messageConfig.formatting) {
                        username = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(user).copy());
                        msg = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(message).copy());
                    }

                    MessageAuthor author = MessageAuthor.of(username, uuid, profile != null ? profile.getName() : username, botEngine.getMinecraftHelper());
                    DiscordMessage discordMessage = new DiscordMessage.Builder(
                            botEngine, MessageType.CHAT
                    )
                    .withMessage(msg)
                    .withAuthor(!fromServer ? author : MessageAuthor.SERVER)
                    .build();

                    discordMessage.sendMessage();
                }
            }
        } catch (Exception e) {
            if (ModConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOG.error("Failed to send message to Discord", e);
            }
        }
    }

    @CraterEventListener
    public void commandEvent(CraterCommandEvent event) {
        if (botEngine == null)
            return;

        String cmd = event.getParseResults().getReader().getString();
        String uuid = null;
        ServerPlayer player = null;
        try {
            player = event.getParseResults().getContext().getLastChild().getSource().getPlayerOrException();
            uuid = ModHelper.INSTANCE.getPlayerSkinUUID(player);
        } catch (CommandSyntaxException ignored) {}

        Component name = Component.literal(event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString());

        String command = cmd.startsWith("/") ? cmd.replaceFirst("/", "") : cmd;
        String cmdName = command.split(" ")[0];
        String username = ModConfig.INSTANCE.messageConfig.formatting ? DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(name).copy()) : ChatFormatting.stripFormatting(name.getString());

        if (username == null) {
            username = "Server";
        }

        if ((cmdName.startsWith("say") || cmdName.startsWith("me")) && ModConfig.INSTANCE.chatConfig.sendSayCommand) {
            String msg = ModUtils.strip(command, "say", "me");
            DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.CHAT)
                    .withAuthor(MessageAuthor.of((player == null ? ModConfig.INSTANCE.webhookConfig.serverName : player.getGameProfile().getName()), uuid == null ? "" : uuid, username, botEngine.getMinecraftHelper()))
                    .withMessage(msg)
                    .build();

            discordMessage.sendMessage();
            return;
        }

        if (ModConfig.INSTANCE.chatConfig.ignoredCommands.contains(cmdName))
            return;

        if (!ModConfig.INSTANCE.chatConfig.broadcastCommands)
            return;

        if (!ModConfig.INSTANCE.messageConfig.relayFullCommands) {
            command = command.split(" ")[0];
        }

        DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.COMMAND)
                .withAuthor(MessageAuthor.SERVER)
                .withMessage(
                        ModConfig.INSTANCE.messageConfig.commands
                                .replace("%player%", username)
                                .replace("%command%", command)
                )
                .build();

        discordMessage.sendMessage();
    }

    @CraterEventListener
    public void playerJoinEvent(CraterPlayerEvent.PlayerLoggedIn event) {
        if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
            if (ModConfig.INSTANCE.chatConfig.joinAndLeaveMessages) {
                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.JOIN_LEAVE)
                        .withMessage(ModConfig.INSTANCE.messageConfig.playerJoined.replace("%player%", event.getPlayer().getDisplayName().getString()))
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void playerLeaveEvent(CraterPlayerEvent.PlayerLoggedOut event) {
        if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
            if (ModConfig.INSTANCE.chatConfig.joinAndLeaveMessages) {
                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.JOIN_LEAVE)
                        .withMessage(ModConfig.INSTANCE.messageConfig.playerLeft.replace("%player%", event.getPlayer().getDisplayName().getString()))
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void onPlayerDeath(CraterLivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (botEngine != null && ModConfig.INSTANCE.generalConfig.enabled) {
                if (ModConfig.INSTANCE.chatConfig.deathMessages) {
                    String msg = ModConfig.INSTANCE.messageConfig.formatting ? DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(event.getDamageSource().getLocalizedDeathMessage(player)).copy()) : ChatFormatting.stripFormatting(event.getDamageSource().getLocalizedDeathMessage(player).getString());

                    DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.DEATH)
                            .withMessage(msg)
                            .withAuthor(MessageAuthor.SERVER)
                            .build();

                    discordMessage.sendMessage();
                }
            }
        }
    }

    @CraterEventListener
    public void onPlayerAdvancement(CraterAdvancementEvent event) {
        try {
            if (botEngine != null && ModConfig.INSTANCE.chatConfig.advancementMessages) {
                String username = ChatFormatting.stripFormatting(event.getPlayer().getDisplayName().getString());
                String finalAdvancement = ChatFormatting.stripFormatting(event.getAdvancement().getDisplay().getTitle().getString());
                String advancementBody = ChatFormatting.stripFormatting(event.getAdvancement().getDisplay().getDescription().getString());

                if (ModConfig.INSTANCE.messageConfig.formatting) {
                    username = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(event.getPlayer().getDisplayName()).copy());
                    finalAdvancement = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(event.getAdvancement().getDisplay().getTitle()).copy());
                    advancementBody = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(event.getAdvancement().getDisplay().getDescription()).copy());
                }

                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.ADVANCEMENT)
                        .withMessage(ModConfig.INSTANCE.messageConfig.achievements.replace("%player%", username).replace("%title%", finalAdvancement).replace("%description%", advancementBody))
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        } catch (Exception e) {
            if (ModConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOG.error("Failed to send advancement to Discord", e);
            }
        }
    }

    // Mod Events
    @CraterEventListener
    public void sdlinkReadyEvent(SDLinkReadyEvent event) {
        if (ModConfig.INSTANCE.messageConfig.sendConsoleMessages) {
            LogReader.init(events.botEngine, ModHelper.INSTANCE.isDevEnv());
        }
    }

    @Override
    public void discordMessageEvent(String s, String s1) {
        if (ModConfig.INSTANCE.generalConfig.debugging) {
            SDLinkConstants.LOG.info("Got message {} from {}", s1, s);
        }
        try {
            MutableComponent component = ModConfig.INSTANCE.messageConfig.formatting ? MinecraftSerializer.INSTANCE.serialize(ModConfig.INSTANCE.chatConfig.mcPrefix.replace("%user%", s) + s1) : Component.literal(ModConfig.INSTANCE.chatConfig.mcPrefix.replace("%user%", s) + s1);
            server.getPlayerList().broadcastSystemMessage(
                    component,
                    false
            );
        } catch (Exception e) {
            if (ModConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOG.error("Failed to send message: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean isWhitelistingEnabled() {
        return server.getPlayerList().isUsingWhitelist();
    }

    @Override
    public boolean whitelistPlayer(MinecraftPlayer player) {
        GameProfile profile = new GameProfile(player.getUuid(), player.getUsername());
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        if (!whiteList.isWhiteListed(profile)) {
            whiteList.add(new UserWhiteListEntry(profile));
            server.getPlayerList().reloadWhiteList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unWhitelistPlayer(MinecraftPlayer player) {
        GameProfile profile = new GameProfile(player.getUuid(), player.getUsername());
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        if (whiteList.isWhiteListed(profile)) {
            whiteList.remove(new UserWhiteListEntry(profile));
            server.getPlayerList().reloadWhiteList();
            kickNonWhitelisted();
            return true;
        } else {
            return false;
        }
    }

    private void kickNonWhitelisted() {
        PlayerList playerlist = server.getPlayerList();
        UserWhiteList whitelist = playerlist.getWhiteList();

        for(ServerPlayer serverplayerentity : Lists.newArrayList(playerlist.getPlayers())) {
            if (!whitelist.isWhiteListed(serverplayerentity.getGameProfile())) {
                serverplayerentity.connection.disconnect(
                        Component.translatable("multiplayer.disconnect.not_whitelisted")
                );
            }
        }
    }

    @Override
    public List<String> getWhitelistedPlayers() {
        return Arrays.stream(server.getPlayerList().getWhiteList().getUserList()).toList();
    }

    @Override
    public boolean isPlayerWhitelisted(MinecraftPlayer player) {
        GameProfile profile = new GameProfile(player.getUuid(), player.getUsername());
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        return whiteList.isWhiteListed(profile);
    }

    @Override
    public int getOnlinePlayerCount() {
        return server.getPlayerCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayers();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        if (server != null && server.getPlayerList() != null) {
            return Arrays.asList(server.getPlayerNames());
        }
        return new ArrayList<>();
    }

    @Override
    public long getServerUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - uptime);
    }

    @Override
    public String getServerVersion() {
        return server.getServerModName() + " - " + server.getServerVersion();
    }

    @Override
    public void executeMcCommand(String s, String s1) {
        String command;
        if (!s1.isEmpty()) {
            command = s.replace("%args%", s1);
        } else {
            command = s.replace(" %args%", "").replace("%args%", "");
        }
        ModHelper.INSTANCE.executeCommand(server, command);
    }

    @Override
    public boolean isOnlineMode() {
        if (ModHelper.INSTANCE.isModLoaded("fabrictailor"))
            return true;

        return server.usesAuthentication();
    }

    // Other
    public BotController getBotEngine() {
        return botEngine;
    }
}
