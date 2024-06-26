package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.api.events.common.CraterPlayerDeathEvent;
import com.hypherionmc.craterlib.api.events.server.*;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.craterlib.core.networking.CraterPacketNetwork;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.craterlib.nojang.world.entity.player.BridgedPlayer;
import com.hypherionmc.craterlib.utils.ChatUtils;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.events.SDLinkReadyEvent;
import com.hypherionmc.sdlink.core.events.VerificationEvent;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.networking.MentionsSyncPacket;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.commands.DiscordCommand;
import com.hypherionmc.sdlink.server.commands.ReloadEmbedsCommand;
import com.hypherionmc.sdlink.server.commands.WhoisCommand;
import com.hypherionmc.sdlink.util.LogReader;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import shadow.kyori.adventure.text.Component;

@Getter
@SuppressWarnings("unused")
public class ServerEvents {

    private BridgedMinecraftServer minecraftServer;
    private final long uptime = System.currentTimeMillis();

    private static ServerEvents events;

    public static ServerEvents getInstance() {
        if (events == null)
            events = new ServerEvents();

        return events;
    }

    private ServerEvents() {
        BotController.newInstance(SDLinkConstants.LOGGER);
        BotController.INSTANCE.initializeBot();
    }

    @CraterEventListener
    public void onCommandRegister(CraterRegisterCommandEvent event) {
        DiscordCommand.register(event);
        ReloadEmbedsCommand.register(event);
        WhoisCommand.register(event);
    }

    @CraterEventListener
    public void onServerStarting(CraterServerLifecycleEvent.Starting event) {
        this.minecraftServer = event.getServer();
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStarting) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.START)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStarting)
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerStarted(CraterServerLifecycleEvent.Started event) {
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStarted) {

            DiscordMessage message = new DiscordMessageBuilder(MessageType.START)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStarted)
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }

        if (BotController.INSTANCE.getJDA().getStatus() == JDA.Status.CONNECTED && CacheManager.getDiscordMembers().isEmpty())
            CacheManager.loadCache();
    }

    @CraterEventListener
    public void onServerStopping(CraterServerLifecycleEvent.Stopping event) {
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStopping) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStopping)
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerStoppedEvent(CraterServerLifecycleEvent.Stopped event) {
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStopped) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStopped)
                    .author(DiscordAuthor.SERVER)
                    .afterSend(() -> BotController.INSTANCE.shutdownBot())
                    .build();

            message.sendMessage();
        } else {
            BotController.INSTANCE.shutdownBot();
        }
    }

    @CraterEventListener
    public void onServerChatEvent(CraterServerChatEvent event) {
        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer())) {
            return;
        }

        onServerChatEvent(event.getComponent(), event.getPlayer().getDisplayName(), SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(event.getPlayer()), event.getPlayer().getGameProfile(), false);
    }

    public void onServerChatEvent(Component message, Component user, String uuid, BridgedGameProfile gameProfile, boolean fromServer) {
        if (user == null || message == null)
            return;

        if (!canSendMessage())
            return;

        try {
            if (SDLinkConfig.INSTANCE.chatConfig.playerMessages) {
                String username = ChatUtils.resolve(user, SDLinkConfig.INSTANCE.chatConfig.formatting);
                String msg = ChatUtils.resolve(message, SDLinkConfig.INSTANCE.chatConfig.formatting);

                if (SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
                    msg = SDLinkChatUtils.parse(msg);
                }

                DiscordAuthor author = DiscordAuthor.of(username, uuid, gameProfile.getName()).setGameProfile(gameProfile);
                DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                        .message(msg)
                        .author(!fromServer ? author : DiscordAuthor.SERVER)
                        .build();

                discordMessage.sendMessage();
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE != null && SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to send Discord Message", e);
            }
        }
    }

    @CraterEventListener
    public void commandEvent(CraterCommandEvent event) {
        if (!canSendMessage())
            return;

        String cmd = event.getCommandString();

        BridgedPlayer player = null;
        String uuid = null;
        Component user = Component.text("Unknown");
        BridgedGameProfile profile = null;
        try {
            player = event.getPlayer();
            uuid = SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(player);
            if (player != null) {
                user = player.getDisplayName();
                profile = player.getGameProfile();
            }
        } catch (Exception ignored) {}

        if (player != null && !SDLinkMCPlatform.INSTANCE.playerIsActive(player))
            return;

        String command = cmd.startsWith("/") ? cmd.replaceFirst("/", "") : cmd;
        String cmdName = command.split(" ")[0];
        String username = ChatUtils.resolve(user, SDLinkConfig.INSTANCE.chatConfig.formatting);

        if (username == null || username.equalsIgnoreCase("unknown")) {
            username = "Server";
        }

        if ((cmdName.equalsIgnoreCase("say") || cmdName.equalsIgnoreCase("me")) && SDLinkConfig.INSTANCE.chatConfig.sendSayCommand) {
            String msg = command;

            if (cmdName.equalsIgnoreCase("me")) {
                msg = ChatUtils.strip(command, "me");
            }

            if (cmdName.equalsIgnoreCase("say")) {
                msg = ChatUtils.strip(command, "say");
            }

            msg = ChatUtils.resolve(Component.text(msg), SDLinkConfig.INSTANCE.chatConfig.formatting);

            DiscordAuthor author = DiscordAuthor.of(
                    username,
                    uuid == null ? "" : uuid,
                    profile != null ? profile.getName() : (player != null ? ChatUtils.resolve(player.getName(), false) : "server")
            );

            if (profile != null)
                author.setGameProfile(profile);

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(author)
                    .message(msg)
                    .build();

            discordMessage.sendMessage();
            return;
        }

        if (cmdName.startsWith("tellraw") && SDLinkConfig.INSTANCE.chatConfig.relayTellRaw) {
            String target = event.getTarget();

            if (!target.equals("@a"))
                return;

            DiscordAuthor author = DiscordAuthor.of(
                    username,
                    uuid == null ? "" : uuid,
                    profile != null ? profile.getName() : (player != null ? ChatUtils.resolve(player.getName(), false) : "server")
            );

            if (profile != null)
                author.setGameProfile(profile);

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(author)
                    .message(ChatUtils.resolve(event.getMessage(), SDLinkConfig.INSTANCE.chatConfig.formatting))
                    .build();

            discordMessage.sendMessage();
            return;
        }

        if (SDLinkConfig.INSTANCE.chatConfig.ignoredCommands.contains(cmdName))
            return;

        if (!SDLinkConfig.INSTANCE.chatConfig.broadcastCommands)
            return;

        if (!SDLinkConfig.INSTANCE.chatConfig.relayFullCommands) {
            command = command.split(" ")[0];
        }

        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.COMMAND)
                .author(DiscordAuthor.SERVER)
                .message(
                        SDLinkConfig.INSTANCE.messageFormatting.commands
                                .replace("%player%", username)
                                .replace("%command%", command)
                )
                .build();

        discordMessage.sendMessage();
    }

    @CraterEventListener
    public void playerJoinEvent(CraterPlayerEvent.PlayerLoggedIn event) {
        // Allow Mentions
        try {
            if (SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
                MentionsSyncPacket packet = new MentionsSyncPacket(CacheManager.getServerRoles(), CacheManager.getServerChannels(), CacheManager.getUserCache());
                CraterPacketNetwork.INSTANCE.getPacketRegistry().sendToClient(packet, event.getPlayer());
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to sync Mentions to Client", e);
            }
        }

        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.playerJoin || !SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.JOIN)
                .message(SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", ChatUtils.resolve(event.getPlayer().getDisplayName(), SDLinkConfig.INSTANCE.chatConfig.formatting)))
                .author(DiscordAuthor.SERVER
                        .setPlayerName(ChatUtils.resolve(event.getPlayer().getDisplayName(), false))
                        .setPlayerAvatar(event.getPlayer().getGameProfile().getName(), event.getPlayer().getStringUUID()))
                .build();

        discordMessage.sendMessage();
    }

    @CraterEventListener
    public void playerLeaveEvent(CraterPlayerEvent.PlayerLoggedOut event) {
        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        if (SDLinkConfig.INSTANCE.accessControl.enabled) {
            try {
                if (SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan) {
                    MinecraftAccount account = MinecraftAccount.of(event.getPlayer().getGameProfile());
                    if (minecraftServer.isPlayerBanned(event.getPlayer().getGameProfile())) {
                        account.banDiscordMember();
                        return;
                    }
                }
            } catch (Exception e) {
                SDLinkConstants.LOGGER.error("Failed to ban, banned discord user", e);
            }
        }

        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerLeave) {
            String name = ChatUtils.resolve(event.getPlayer().getDisplayName(), SDLinkConfig.INSTANCE.chatConfig.formatting);

            DiscordMessage message = new DiscordMessageBuilder(MessageType.LEAVE)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", name))
                    .author(DiscordAuthor.SERVER
                            .setPlayerName(ChatUtils.resolve(event.getPlayer().getDisplayName(), false))
                            .setPlayerAvatar(event.getPlayer().getGameProfile().getName(), SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(event.getPlayer())))
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onPlayerDeath(CraterPlayerDeathEvent event) {
        if (event.getPlayer().isServerPlayer() && !SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        BridgedPlayer player = event.getPlayer();

        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.deathMessages) {
            String name = ChatUtils.resolve(player.getDisplayName(), SDLinkConfig.INSTANCE.chatConfig.formatting);
            String msg = ChatUtils.resolve(event.getDeathMessage(), SDLinkConfig.INSTANCE.chatConfig.formatting);

            if (msg.startsWith(name + " ")) {
                msg = msg.substring((name + " ").length());
            }

            DiscordMessage message = new DiscordMessageBuilder(MessageType.DEATH)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.death.replace("%player%", name).replace("%message%", msg))
                    .author(DiscordAuthor.SERVER
                            .setPlayerName(ChatUtils.resolve(player.getDisplayName(), false))
                            .setPlayerAvatar(player.getGameProfile().getName(), player.getStringUUID()))
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onPlayerAdvancement(CraterAdvancementEvent event) {
        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        try {
            if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.advancementMessages) {
                String username = ChatUtils.resolve(event.getPlayer().getDisplayName(), SDLinkConfig.INSTANCE.chatConfig.formatting);
                String finalAdvancement = ChatUtils.resolve(event.getTitle(), SDLinkConfig.INSTANCE.chatConfig.formatting);
                String advancementBody = ChatUtils.resolve(event.getDescription(), SDLinkConfig.INSTANCE.chatConfig.formatting);

                DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.ADVANCEMENT)
                        .message(SDLinkConfig.INSTANCE.messageFormatting.achievements.replace("%player%", username).replace("%title%", finalAdvancement).replace("%description%", advancementBody))
                        .author(DiscordAuthor.SERVER
                                .setPlayerName(ChatUtils.resolve(event.getPlayer().getDisplayName(), false))
                                .setPlayerAvatar(event.getPlayer().getGameProfile().getName(), event.getPlayer().getStringUUID()))
                        .build();

                discordMessage.sendMessage();
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to send advancement to Discord", e);
            }
        }
    }

    @CraterEventListener
    public void preLoginEvent(PlayerPreLoginEvent event) {
        if (BotController.INSTANCE == null || !BotController.INSTANCE.isBotReady())
            return;

        if (SDLinkConfig.INSTANCE.accessControl.enabled) {
            MinecraftAccount account = MinecraftAccount.of(event.getGameProfile());

            try {
                if (SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan) {
                    if (minecraftServer.isPlayerBanned(event.getGameProfile())) {
                        account.banDiscordMember();
                        return;
                    }
                }
            } catch (Exception e) {
                SDLinkConstants.LOGGER.error("Failed to ban, banned discord user", e);
            }

            var result = account.canLogin();

            if (result.isError())
                event.setMessage(Component.text(result.getMessage()));
        }
    }

    @CraterEventListener
    public void serverBroadcastEvent(MessageBroadcastEvent event) {
        String thread = event.getThreadName();

        if (thread.startsWith("net.minecraft") || thread.contains("com.hypherionmc"))
            return;

        if (SDLinkConfig.INSTANCE.ignoreConfig.enabled) {
            if (SDLinkConfig.INSTANCE.ignoreConfig.ignoredThread.stream().anyMatch(thread::startsWith))
                return;
        }

        if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
            SDLinkConstants.LOGGER.info("Relaying message from {}", thread);
        }

        try {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.CHAT).author(DiscordAuthor.SERVER).message(ChatUtils.resolve(event.getComponent(), SDLinkConfig.INSTANCE.chatConfig.formatting)).build();
            message.sendMessage();
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to broadcast message", e);
            }
        }
    }

    @CraterEventListener
    public void sdlinkReadyEvent(SDLinkReadyEvent event) {
        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages)
            LogReader.init(ModloaderEnvironment.INSTANCE.isDevEnv());
    }

    @CraterEventListener
    public void playerVerified(VerificationEvent.PlayerVerified event) {
        if (!minecraftServer.isUsingWhitelist())
            return;

        try {
            BridgedGameProfile p = BridgedGameProfile.mojang(event.getAccount().getUuid(), event.getAccount().getUsername());
            minecraftServer.whitelistPlayer(p);
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to whitelist verified player", e);
        }
    }

    @CraterEventListener
    public void playerUnVerified(VerificationEvent.PlayerUnverified event) {
        if (!minecraftServer.isUsingWhitelist())
            return;

        try {
            BridgedGameProfile p = BridgedGameProfile.mojang(event.getAccount().getUuid(), event.getAccount().getUsername());
            minecraftServer.unWhitelistPlayer(p);
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to unwhitelist verified player", e);
        }
    }

    public boolean canSendMessage() {
        return BotController.INSTANCE != null && BotController.INSTANCE.isBotReady() && SDLinkConfig.INSTANCE != null;
    }

}
