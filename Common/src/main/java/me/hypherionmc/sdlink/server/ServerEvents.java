package me.hypherionmc.sdlink.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import me.hypherionmc.mcdiscordformatter.discord.DiscordSerializer;
import me.hypherionmc.mcdiscordformatter.minecraft.MinecraftSerializer;
import me.hypherionmc.sdlink.SDLinkConstants;
import me.hypherionmc.sdlink.platform.PlatformHelper;
import me.hypherionmc.sdlink.server.commands.DiscordCommand;
import me.hypherionmc.sdlink.server.commands.ReloadModCommand;
import me.hypherionmc.sdlink.server.commands.WhoisCommand;
import me.hypherionmc.sdlink.util.ModUtils;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.discord.DiscordMessage;
import me.hypherionmc.sdlinklib.discord.messages.MessageAuthor;
import me.hypherionmc.sdlinklib.discord.messages.MessageDestination;
import me.hypherionmc.sdlinklib.discord.messages.MessageType;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.LogReader;
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

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
    public void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
        DiscordCommand.register(dispatcher);
        WhoisCommand.register(dispatcher);
        ReloadModCommand.register(dispatcher);
    }

    public void onServerStarting(MinecraftServer server) {
        this.server = server;
        if (botEngine != null && modConfig.generalConfig.enabled) {
            if (modConfig.chatConfig.serverStarting) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(modConfig.messageConfig.serverStarting)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
        }
    }

    public void onServerStarted() {
        if (botEngine != null && modConfig.generalConfig.enabled) {
            botEngine.checkWhitelisting();
            if (modConfig.chatConfig.serverStarted) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(modConfig.messageConfig.serverStarted)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
        }

        if (modConfig.messageConfig.sendConsoleMessages) {
            LogReader.init(botEngine, PlatformHelper.MOD_HELPER.isDevEnv());
        }
    }

    public void onServerStopping() {
        if (botEngine != null && modConfig.generalConfig.enabled) {
            if (modConfig.chatConfig.serverStopping) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(modConfig.messageConfig.serverStopping)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
        }
    }

    public void onServerStoppedEvent() {
        if (botEngine != null && modConfig.generalConfig.enabled) {
            if (modConfig.chatConfig.serverStopped) {
                DiscordMessage message = new DiscordMessage.Builder(
                        botEngine,
                        MessageType.START_STOP
                )
                .withMessage(modConfig.messageConfig.serverStopped)
                .withAuthor(MessageAuthor.SERVER)
                .build();

                message.sendMessage();
            }
            botEngine.shutdownBot();
        }
    }

    public void onServerChatEvent(Component component, Component user, String uuid) {
        onServerChatEvent(component, user, uuid, null, false);
    }

    public void onServerChatEvent(Component component, Component user, GameProfile profile, String uuid) {
        onServerChatEvent(component, user, uuid, profile, false);
    }

    public void onServerChatEvent(Component message, Component user, String uuid, GameProfile profile, boolean fromServer) {
        try {
            if (botEngine != null && modConfig.generalConfig.enabled) {
                if (modConfig.chatConfig.playerMessages) {
                    String username = ChatFormatting.stripFormatting(user.getString());
                    String msg = ChatFormatting.stripFormatting(message.getString());

                    if (modConfig.messageConfig.formatting) {
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
            if (modConfig.generalConfig.debugging) {
                SDLinkConstants.LOG.error("Failed to send message to Discord", e);
            }
        }
    }

    public void commandEvent(String cmd, Component name, String uuid, GameProfile profile) {
        if (botEngine == null)
            return;

        String command = cmd.startsWith("/") ? cmd.replaceFirst("/", "") : cmd;
        String cmdName = command.split(" ")[0];
        String username = modConfig.messageConfig.formatting ? DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(name).copy()) : ChatFormatting.stripFormatting(name.getString());

        if (username == null) {
            username = "Server";
        }

        if ((cmdName.startsWith("say") || cmdName.startsWith("me")) && modConfig.chatConfig.sendSayCommand) {
            String msg = ModUtils.strip(command, "say", "me");
            DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.CHAT)
                    .withAuthor(MessageAuthor.of(username, uuid == null ? "" : uuid, profile != null ? profile.getName() : username, botEngine.getMinecraftHelper()))
                    .withMessage(msg)
                    .build();

            discordMessage.sendMessage();
            return;
        }

        if (modConfig.chatConfig.ignoredCommands.contains(cmdName))
            return;

        if (!modConfig.chatConfig.broadcastCommands)
            return;

        if (!modConfig.messageConfig.relayFullCommands) {
            command = command.split(" ")[0];
        }

        DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.COMMAND)
                .withAuthor(MessageAuthor.SERVER)
                .withMessage(
                        modConfig.messageConfig.commands
                                .replace("%player%", username)
                                .replace("%command%", command)
                )
                .build();

        discordMessage.sendMessage();
    }

    public void playerJoinEvent(Player player) {
        if (botEngine != null && modConfig.generalConfig.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.JOIN_LEAVE)
                        .withMessage(modConfig.messageConfig.playerJoined.replace("%player%", player.getDisplayName().getString()))
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        }
    }

    public void playerLeaveEvent(Player player) {
        if (botEngine != null && modConfig.generalConfig.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.JOIN_LEAVE)
                        .withMessage(modConfig.messageConfig.playerLeft.replace("%player%", player.getDisplayName().getString()))
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        }
    }

    public void onPlayerDeath(Player player, Component message) {
        if (botEngine != null && modConfig.generalConfig.enabled) {
            if (modConfig.chatConfig.deathMessages) {
                String msg = modConfig.messageConfig.formatting ? DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(message).copy()) : ChatFormatting.stripFormatting(message.getString());

                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.DEATH)
                        .withMessage(msg)
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        }
    }

    public void onPlayerAdvancement(Component name, Component advancement, Component advancement_description) {
        try {
            if (botEngine != null && modConfig.chatConfig.advancementMessages) {
                String username = ChatFormatting.stripFormatting(name.getString());
                String finalAdvancement = ChatFormatting.stripFormatting(advancement.getString());
                String advancementBody = ChatFormatting.stripFormatting(advancement_description.getString());

                if (modConfig.messageConfig.formatting) {
                    username = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(name).copy());
                    finalAdvancement = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(advancement).copy());
                    advancementBody = DiscordSerializer.INSTANCE.serialize(ModUtils.safeCopy(advancement_description).copy());
                }

                DiscordMessage discordMessage = new DiscordMessage.Builder(botEngine, MessageType.ADVANCEMENT)
                        .withMessage(modConfig.messageConfig.achievements.replace("%player%", username).replace("%title%", finalAdvancement).replace("%description%", advancementBody))
                        .withAuthor(MessageAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        } catch (Exception e) {
            if (modConfig.generalConfig.debugging) {
                SDLinkConstants.LOG.error("Failed to send advancement to Discord", e);
            }
        }
    }

    // Mod Events

    @Override
    public void discordMessageEvent(String s, String s1) {
        if (modConfig.generalConfig.debugging) {
            SDLinkConstants.LOG.info("Got message {} from {}", s1, s);
        }
        try {
            MutableComponent component = modConfig.messageConfig.formatting ? MinecraftSerializer.INSTANCE.serialize(modConfig.chatConfig.mcPrefix.replace("%user%", s) + s1) : Component.literal(modConfig.chatConfig.mcPrefix.replace("%user%", s) + s1);
            server.getPlayerList().broadcastSystemMessage(
                    component,
                    false
            );
        } catch (Exception e) {
            if (modConfig.generalConfig.debugging) {
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
        PlatformHelper.MOD_HELPER.executeCommand(server, command);
    }

    @Override
    public boolean isOnlineMode() {
        if (PlatformHelper.MOD_HELPER.isModLoaded("fabrictailor"))
            return true;

        return server.usesAuthentication();
    }

    // Other
    public BotController getBotEngine() {
        return botEngine;
    }

    public ModConfig getModConfig() {
        return modConfig;
    }
}
