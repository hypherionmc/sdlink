package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.api.events.common.CraterPlayerDeathEvent;
import com.hypherionmc.craterlib.api.events.compat.PlayerRevivedEvent;
import com.hypherionmc.craterlib.api.events.server.*;
import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile;
import com.hypherionmc.craterlib.api.game.server.CraterGameServer;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.craterlib.api.game.world.entity.player.CraterPlayer;
import com.hypherionmc.craterlib.api.game.world.level.CraterCommonGameRules;
import com.hypherionmc.craterlib.api.loader.CraterCompat;
import com.hypherionmc.craterlib.api.loader.CraterLoader;
import com.hypherionmc.craterlib.api.loader.LoaderType;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.craterlib.core.networking.CraterPacketNetwork;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.accounts.DiscordUser;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.events.SDLinkReadyEvent;
import com.hypherionmc.sdlink.api.events.VerificationEvent;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.compat.rolesync.RoleSync;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.SDLinkRelayConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.experimental.ExperimentalFeatures;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.core.relay.DataMessage;
import com.hypherionmc.sdlink.core.relay.RelayMessage;
import com.hypherionmc.sdlink.core.relay.SDLinkRelayClient;
import com.hypherionmc.sdlink.networking.MentionsSyncPacket;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.commands.*;
import com.hypherionmc.sdlink.util.Debugger;
import com.hypherionmc.sdlink.util.LogReader;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.hypherionmc.sdlink.util.translations.SDText;
import io.github.joagar21.guilds.api.GuildsAPI;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("unused")
public final class ServerEvents {

    private CraterGameServer minecraftServer;
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

    public static void reloadBot(boolean isReload) {
        BotController.reloadInstance(isReload);
    }

    @CraterEventListener
    public void onCommandRegister(CraterRegisterCommandEvent event) {
        DiscordCommand.register(event);
        ReloadEmbedsCommand.register(event);
        WhoisCommand.register(event);
        ReloadBotCommand.register(event);
        HidePlayerCommand.register(event);
        UnhidePlayerCommand.register(event);
        ConfigEditorCommand.register(event);
        DiscordVerifyCommand.register(event);
    }

    @CraterEventListener
    public void onServerStarting(CraterServerLifecycleEvent.Starting event) {
        Debugger.INSTANCE.log("Running Starting Event");
        this.minecraftServer = event.getServer();
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStarting) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.START)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStarting)
                    .author(DiscordAuthor.getServer())
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerStarted(CraterServerLifecycleEvent.Started event) {
        Debugger.INSTANCE.log("Running Started Event");
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStarted) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.START)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStarted)
                    .author(DiscordAuthor.getServer())
                    .build();

            message.sendMessage();
        }

        if (BotController.INSTANCE != null && BotController.INSTANCE.isBotReady() && CacheManager.getDiscordMembers().isEmpty())
            CacheManager.loadCache();

        if (CraterLoader.isModLoaded("utilitarian")) {
            BotController.INSTANCE.getLogger().warn("Utilitarian Mod Detected. If your discord messages are missing from in-game, please check that the word Discord is not blocked in config/utilitarian.json. This applies mostly to newer FTB Modpacks");
        }

        if (SDLinkCompatConfig.INSTANCE.common.ignoreCancelled) {
            BotController.INSTANCE.getLogger().warn("Cancelled chat events are set to be ignored. If you are missing chat messages from Minecraft -> Discord, this is probably why.");
        }
    }

    @CraterEventListener
    public void onServerStopping(CraterServerLifecycleEvent.Stopping event) {
        Debugger.INSTANCE.log("Running server stopping event");
        Debugger.INSTANCE.log("Can Run: {}", canSendMessage());
        Debugger.INSTANCE.log("Event Enabled: {}", SDLinkConfig.INSTANCE.chatConfig.serverStopping);

        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStopping) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStopping)
                    .author(DiscordAuthor.getServer())
                    .afterSend(() -> {
                        // Stop Log Relay
                        try {
                            LogReader.destroy();
                        } catch (Exception ignored) {}
                    })
                    .build();

            message.sendMessage(true);
        }
    }

    @CraterEventListener
    public void onServerStoppedEvent(CraterServerLifecycleEvent.Stopped event) {
        Debugger.INSTANCE.log("Running server stopped event");
        Debugger.INSTANCE.log("Can Run: {}", canSendMessage());
        Debugger.INSTANCE.log("Event Enabled: {}", SDLinkConfig.INSTANCE.chatConfig.serverStopping);

        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStopped) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStopped)
                    .author(DiscordAuthor.getServer())
                    .afterSend(() -> BotController.INSTANCE.shutdownBot(false))
                    .build();

            message.sendMessage(true);
        } else {
            BotController.INSTANCE.shutdownBot(false);
        }
    }

    @CraterEventListener
    public void onServerChatEvent(CraterServerChatEvent event) {
        boolean ignoreCancelled = ExperimentalFeatures.INSTANCE.IGNORE_CANCELLED_CHAT || SDLinkCompatConfig.INSTANCE.common.ignoreCancelled;

        if (event.getSource() == CraterServerChatEvent.MessageSource.MAIN && ignoreCancelled) {
            Debugger.INSTANCE.log("Ignoring Chat Event due to config");
            return;
        }

        if (event.getSource() == CraterServerChatEvent.MessageSource.BACKUP && !ignoreCancelled) {
            Debugger.INSTANCE.log("Ignoring Backup Chat Event due to config");
            return;
        }

        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer())) {
            Debugger.INSTANCE.log("Ignoring chat because player is vanished");
            return;
        }

        if (HiddenPlayersManager.INSTANCE.isPlayerHidden(event.getPlayer().getStringUUID())) {
            Debugger.INSTANCE.log("Ignoring chat because hidden player is muted");
            return;
        }

        // Cobblemon Guilds
        if (SDLinkCompatConfig.INSTANCE.common.cobblemonguilds && CraterLoader.isModLoaded("guilds")) {
            if (GuildsAPI.getEnabledGuildChat().contains(event.getPlayer().getUUID())) return;
        }

        // FTB Essentials
        if (SDLinkCompatConfig.INSTANCE.common.ftbessentials && CraterLoader.isModLoaded("ftbessentials") && CraterCompat.isPlayerMuted(event.getPlayer())) {
            Debugger.INSTANCE.log("Ignoring chat because player is FTB Essentials Muted");
            return;
        }

        // Advanced Chat
        if (CraterLoader.isModLoaded("advanced-chat") && CraterCompat.isPrivateMessage(event.getPlayer())) {
            Debugger.INSTANCE.log("Ignoring chat because message is Advanced Chat private message");
            return;
        }

        onServerChatEvent(event.getComponent(), event.getPlayer().getDisplayName(), SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(event.getPlayer()), event.getPlayer().getGameProfile(), false);
    }

    public void onServerChatEvent(Text message, Text user, String uuid, CraterGameProfile gameProfile, boolean fromServer) {
        if (user == null || message == null)
            return;

        if (!canSendMessage())
            return;

        try {
            if (SDLinkConfig.INSTANCE.chatConfig.playerMessages) {
                String username = user.asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                String msg = message.asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

                Debugger.INSTANCE.log("Username is {}", username);
                Debugger.INSTANCE.log("Message is {}", msg);

                if (SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
                    msg = SDLinkChatUtils.parse(msg);
                    Debugger.INSTANCE.log("Message after ClientMentionParse is {}", msg);
                }

                msg = parseChatMentions(msg);
                Debugger.INSTANCE.log("Message after ChatMentionParse is {}", msg);

                DiscordAuthor author = DiscordAuthor.of(username, uuid, gameProfile.getName())
                        .setGameProfile(gameProfile).setPlayerName(gameProfile.getName())
                        .setPlayerAvatar(gameProfile.getName(), uuid);

                DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                        .message(msg)
                        .author(!fromServer ? author : DiscordAuthor.getServer())
                        .build();

                Debugger.INSTANCE.log("Now Sending Message");
                discordMessage.sendMessage();

                if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayMinecraftChats) {
                    RelayMessage newRelay = RelayMessage.of(
                            RelayMessage.MessageType.CHAT,
                            SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                            DataMessage.of(user, gameProfile.getName(), message, gameProfile.getId(), null, false)
                    );

                    SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
                }
            }
        } catch (Exception e) {
            Debugger.INSTANCE.log("Failed to send Discord Message", e);
        }
    }

    @CraterEventListener
    public void commandEvent(CraterCommandEvent event) {
        if (!canSendMessage())
            return;

        String cmd = event.getCommandString();

        if (cmd.equalsIgnoreCase("reloadbot"))
            return;

        CraterPlayer player = null;
        String uuid = null;
        Text user = Text.literal(SDText.translate("error.unknown").toString());
        CraterGameProfile profile = null;

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

        String command = Text.strip(cmd, "/");
        String cmdName = command.split(" ")[0];
        String username = user.asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

        if (username == null || username.equalsIgnoreCase("unknown")) {
            username = "Server";
        }

        if (username.equalsIgnoreCase("sdlinktriggersystem") || username.equalsIgnoreCase(""))
            return;

        DiscordAuthor author = DiscordAuthor.of(
                username,
                uuid == null ? "" : uuid,
                profile != null ? profile.getName() : (player != null ? player.getName().asString() : "server")
        );

        if (profile != null)
            author.setGameProfile(profile);

        if ((cmdName.equalsIgnoreCase("say") || cmdName.equalsIgnoreCase("me")) && SDLinkConfig.INSTANCE.chatConfig.sendSayCommand) {
            String msg = command;

            if (cmdName.equalsIgnoreCase("me")) {
                msg = Text.strip(command, "me");
            }

            if (cmdName.equalsIgnoreCase("say")) {
                msg = Text.strip(command, "say");
            }

            msg = Text.literal(msg).asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(author)
                    .message(msg)
                    .build();

            discordMessage.sendMessage();

            if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayMinecraftChats) {
                RelayMessage newRelay = RelayMessage.of(
                        RelayMessage.MessageType.CHAT,
                        SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                        DataMessage.of(
                                event.getPlayer() == null ? Text.literal("Server") : event.getPlayer().getDisplayName(),
                                event.getPlayer() == null ? "server" : event.getPlayer().getGameProfile().getName(),
                                Text.literal(msg),
                                event.getPlayer() == null ? UUID.randomUUID() : event.getPlayer().getUUID(),
                                event.getPlayer() == null
                        )
                );

                SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
            }
            return;
        }

        if (cmdName.startsWith("tellraw") && SDLinkConfig.INSTANCE.chatConfig.relayTellRaw) {
            String target = event.getTarget();

            if (!target.equals("@a"))
                return;

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(author)
                    .message(event.getMessage().asString(SDLinkConfig.INSTANCE.chatConfig.formatting))
                    .build();

            discordMessage.sendMessage();
            return;
        }

        if (cmdName.equalsIgnoreCase("ftbteams") && command.split(" ")[1].startsWith("chat") && SDLinkCompatConfig.INSTANCE.common.ftbteams_chat) {
            String msg = Text.strip(command, "ftbteams chat");
            msg = Text.literal(msg).asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(author)
                    .message(msg)
                    .build();

            discordMessage.sendMessage();

            if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayMinecraftChats) {
                RelayMessage newRelay = RelayMessage.of(
                        RelayMessage.MessageType.CHAT,
                        SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                        DataMessage.of(
                                event.getPlayer() == null ? Text.literal("Server") : event.getPlayer().getDisplayName(),
                                event.getPlayer() == null ? "server" : event.getPlayer().getGameProfile().getName(),
                                Text.literal(msg),
                                event.getPlayer() == null ? UUID.randomUUID() : event.getPlayer().getUUID(),
                                event.getPlayer() == null
                        )
                );

                SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
            }
            return;
        }

        boolean sendImmediately = cmd.equalsIgnoreCase("stop");

        if (SDLinkConfig.INSTANCE.chatConfig.ignoredCommands.contains(cmdName) || !SDLinkConfig.INSTANCE.chatConfig.broadcastCommands)
            return;

        if (!SDLinkConfig.INSTANCE.chatConfig.relayFullCommands) {
            command = command.split(" ")[0];
        }

        if (event.getPlayer() != null && SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
            MinecraftAccount mcAccount = MinecraftAccount.of(event.getPlayer().getGameProfile());
            DiscordUser discordUser = mcAccount.getDiscordUser();

            if (discordUser != null) {
                username = discordUser.getEffectiveName();
            }
        }

        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.COMMANDS)
                .author(author)
                .message(
                        SDLinkConfig.INSTANCE.messageFormatting.commands
                                .replace("%player%", username)
                                .replace("%command%", command)
                )
                .build();

        discordMessage.sendMessage(sendImmediately);
    }

    @CraterEventListener
    public void playerJoinEvent(CraterPlayerEvent.PlayerLoggedIn event) {
        // Allow Mentions
        try {
            if (SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat && CraterLoader.getLoaderType() != LoaderType.PAPER) {
                MentionsSyncPacket packet = new MentionsSyncPacket(CacheManager.getServerRoles(), CacheManager.getServerChannels(), CacheManager.getUserCache());
                CraterPacketNetwork.INSTANCE.getPacketRegistry().sendToClient(packet, event.getPlayer());
            }
        } catch (Exception e) {
            Debugger.INSTANCE.log("Failed to sync Mentions to Client", e);
        }

        SDLinkAccount account = DatabaseManager.INSTANCE.findById(event.getPlayer().getStringUUID(), SDLinkAccount.class);

        if (account != null) {
            account.setInGameName(event.getPlayer().getDisplayName().asString());
            DatabaseManager.INSTANCE.updateEntry(account);
        }

        String playerName = event.getPlayer().getDisplayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

        MinecraftAccount mcAccount = MinecraftAccount.of(event.getPlayer().getGameProfile());
        DiscordUser discordUser = mcAccount.getDiscordUser();

        if (discordUser != null && SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
            playerName = discordUser.getEffectiveName();
        }

        RoleSync.INSTANCE.sync(event.getPlayer());

        if (event.isFromVanish() && !SDLinkCompatConfig.INSTANCE.vanishCompat.sendFakeJoinLeaveMessage)
            return;

        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.playerJoin || (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()) && !event.isFromVanish()))
            return;

        String msg = SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", playerName);

        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.JOIN)
                .message(msg)
                .author(DiscordAuthor.getServer()
                        .setPlayerName(event.getPlayer().getName().asString()).setGameProfile(event.getPlayer().getGameProfile())
                        .setPlayerAvatar(event.getPlayer().getGameProfile().getName(), event.getPlayer().getStringUUID()))
                .build();

        discordMessage.sendMessage();

        if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayJoinMessages) {
            RelayMessage newRelay = RelayMessage.of(
                    RelayMessage.MessageType.JOIN,
                    SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                    DataMessage.of(
                            event.getPlayer().getDisplayName(),
                            event.getPlayer().getGameProfile().getName(),
                            null,
                            event.getPlayer().getUUID(),
                            false
                    )
            );

            SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
        }
    }

    @CraterEventListener
    public void playerLeaveEvent(CraterPlayerEvent.PlayerLoggedOut event) {
        if ((SDLinkConfig.INSTANCE.accessControl.enabled || SDLinkConfig.INSTANCE.accessControl.optionalVerification)) {
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

        if (event.isFromVanish() && !SDLinkCompatConfig.INSTANCE.vanishCompat.sendFakeJoinLeaveMessage)
            return;

        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.playerLeave || (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()) && !event.isFromVanish()))
            return;

        String playerName = event.getPlayer().getDisplayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

        MinecraftAccount mcAccount = MinecraftAccount.of(event.getPlayer().getGameProfile());
        DiscordUser discordUser = mcAccount.getDiscordUser();

        if (discordUser != null && SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
            playerName = discordUser.getEffectiveName();
        }

        String msg = SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", playerName);

        DiscordMessage message = new DiscordMessageBuilder(MessageType.LEAVE)
                .message(msg)
                .author(DiscordAuthor.getServer()
                        .setPlayerName(event.getPlayer().getName().asString()).setGameProfile(event.getPlayer().getGameProfile())
                        .setPlayerAvatar(event.getPlayer().getGameProfile().getName(), SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(event.getPlayer())))
                .build();

        message.sendMessage();

        if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayLeaveMessages) {
            RelayMessage newRelay = RelayMessage.of(
                    RelayMessage.MessageType.LEAVE,
                    SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                    DataMessage.of(
                            event.getPlayer().getDisplayName(),
                            event.getPlayer().getGameProfile().getName(),
                            null,
                            event.getPlayer().getUUID(),
                            false
                    )
            );

            SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
        }
    }

    @CraterEventListener
    public void onPlayerDeath(CraterPlayerDeathEvent event) {
        if (event.getPlayer().isServerPlayer() && !SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        if (!minecraftServer.getGameRules().getBoolean(CraterCommonGameRules.RULE_SHOWDEATHMESSAGES) && SDLinkConfig.INSTANCE.chatConfig.deathMessages.followGameRule())
            return;

        CraterPlayer player = event.getPlayer();

        if (canSendMessage()) {
            String name = player.getDisplayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
            String msg = event.getDamageSource().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
            String finalMessage = SDLinkConfig.INSTANCE.messageFormatting.death;

            if (SDLinkCompatConfig.INSTANCE.playerReviveCompat.enabled && CraterLoader.isModLoaded("playerrevive")) {
                if (!CraterCompat.isPlayerBleeding(player) && !CraterCompat.playerBledOut(player)) {
                    finalMessage = SDLinkCompatConfig.INSTANCE.playerReviveCompat.reviveWaitingMessage;
                }

                if (CraterCompat.playerBledOut(player)) {
                    finalMessage = SDLinkCompatConfig.INSTANCE.playerReviveCompat.playerBledOutMessage;
                }
            }

            if (msg.startsWith(name + " ")) {
                msg = msg.substring((name + " ").length());
            }

            if (SDLinkConfig.INSTANCE.chatConfig.deathMessages.isFalse()) {
                return;
            }

            MinecraftAccount mcAccount = MinecraftAccount.of(event.getPlayer().getGameProfile());
            DiscordUser discordUser = mcAccount.getDiscordUser();

            if (discordUser != null && SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
                name = discordUser.getEffectiveName();
            }

            finalMessage = finalMessage.replace("%player%", name).replace("%message%", msg);

            DiscordMessage message = new DiscordMessageBuilder(MessageType.DEATH)
                    .message(finalMessage)
                    .author(DiscordAuthor.getServer()
                            .setGameProfile(event.getPlayer().getGameProfile())
                            .setPlayerName(player.getDisplayName().asString())
                            .setPlayerAvatar(player.getGameProfile().getName(), player.getStringUUID()))
                    .build();

            message.sendMessage();

            if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayDeathMessages) {
                RelayMessage newRelay = RelayMessage.of(
                        RelayMessage.MessageType.DEATH,
                        SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                        DataMessage.of(
                                event.getPlayer().getDisplayName(),
                                event.getPlayer().getGameProfile().getName(),
                                event.getDamageSource(),
                                event.getPlayer().getUUID(),
                                false)
                );

                SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
            }
        }
    }

    @CraterEventListener
    public void onPlayerAdvancement(CraterAdvancementEvent event) {
        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        if (!minecraftServer.getGameRules().getBoolean(CraterCommonGameRules.RULE_ANNOUNCE_ADVANCEMENTS) && SDLinkConfig.INSTANCE.chatConfig.advancementMessages.followGameRule())
            return;

        try {
            if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.advancementMessages.isTrue()) {
                String username = event.getPlayer().getDisplayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                String finalAdvancement = event.getTitle().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
                String advancementBody = event.getDescription().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);

                MinecraftAccount mcAccount = MinecraftAccount.of(event.getPlayer().getGameProfile());
                DiscordUser discordUser = mcAccount.getDiscordUser();

                if (discordUser != null && SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
                    username = discordUser.getEffectiveName();
                }

                String msg = SDLinkConfig.INSTANCE.messageFormatting.achievements.replace("%player%", username).replace("%title%", finalAdvancement).replace("%description%", advancementBody);

                DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.ADVANCEMENTS)
                        .message(msg)
                        .author(DiscordAuthor.getServer()
                                .setGameProfile(event.getPlayer().getGameProfile())
                                .setPlayerName(event.getPlayer().getDisplayName().asString())
                                .setPlayerAvatar(event.getPlayer().getGameProfile().getName(), event.getPlayer().getStringUUID()))
                        .build();

                discordMessage.sendMessage();

                if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayAdvancementMessages) {
                    RelayMessage newRelay = RelayMessage.of(
                            RelayMessage.MessageType.ADVANCEMENT,
                            SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                            DataMessage.of(
                                    event.getPlayer().getDisplayName(),
                                    event.getPlayer().getGameProfile().getName(),
                                    event.getTitle(),
                                    event.getPlayer().getUUID(),
                                    event.getDescription(),
                                    false)
                    );

                    SDLinkRelayClient.INSTANCE.relayMessage(newRelay);
                }
            }
        } catch (Exception e) {
            Debugger.INSTANCE.log("Failed to send advancement to Discord", e);
        }
    }

    @CraterEventListener
    public void preLoginEvent(PlayerPreLoginEvent event) {
        if (BotController.INSTANCE == null || !BotController.INSTANCE.isBotReady())
            return;

        if (SDLinkConfig.INSTANCE.accessControl.enabled || SDLinkConfig.INSTANCE.accessControl.optionalVerification) {
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
                event.setMessage(Text.formatted(result.getMessage()));
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

        Debugger.INSTANCE.log("Relaying message from {}", thread);

        try {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.CHAT).author(DiscordAuthor.getServer()).message(event.getComponent().asString(SDLinkConfig.INSTANCE.chatConfig.formatting)).build();
            message.sendMessage();
        } catch (Exception e) {
            Debugger.INSTANCE.log("Failed to broadcast message", e);
        }
    }

    @CraterEventListener
    public void sdlinkReadyEvent(SDLinkReadyEvent event) {
        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages)
            LogReader.init(CraterLoader.isDevEnv());
    }

    @CraterEventListener
    public void playerVerified(VerificationEvent.PlayerVerified event) {
        if (!minecraftServer.isUsingWhitelist())
            return;

        try {
            CraterGameProfile p = CraterGameProfile.fromGame(event.getAccount().getUsername(), event.getAccount().getUuid());
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
            CraterGameProfile p = CraterGameProfile.fromGame(event.getAccount().getUsername(), event.getAccount().getUuid());
            minecraftServer.unWhitelistPlayer(p);
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to unwhitelist verified player", e);
        }
    }

    public boolean canSendMessage() {
        return BotController.INSTANCE != null && BotController.INSTANCE.isBotReady() && SDLinkConfig.INSTANCE != null;
    }

    @CraterEventListener
    public void playerRevivedEvent(PlayerRevivedEvent event) {
        if (!canSendMessage() || !SDLinkCompatConfig.INSTANCE.playerReviveCompat.enabled)
            return;

        CraterPlayer player = event.getPlayer();
        String name = player.getDisplayName().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
        String finalMessage = SDLinkCompatConfig.INSTANCE.playerReviveCompat.revivedMessage;

        MinecraftAccount mcAccount = MinecraftAccount.of(event.getPlayer().getGameProfile());
        DiscordUser discordUser = mcAccount.getDiscordUser();

        if (discordUser != null && SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
            name = discordUser.getEffectiveName();
        }

        DiscordMessage message = new DiscordMessageBuilder(MessageType.DEATH)
                .message(finalMessage.replace("%player%", name))
                .author(DiscordAuthor.getServer()
                        .setGameProfile(event.getPlayer().getGameProfile())
                        .setPlayerName(player.getDisplayName().asString())
                        .setPlayerAvatar(player.getGameProfile().getName(), player.getStringUUID()))
                .build();

        message.sendMessage();
    }

    @CraterEventListener
    public void userWhitelisted(WhitelistChangedEvent.EntryAdded event) {
        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.whitelistChanged)
            return;

        DiscordMessage message = new DiscordMessageBuilder(MessageType.WHITELIST)
                .message(SDLinkConfig.INSTANCE.messageFormatting.whitelistAdded.replace("%player%", event.getProfile().getName()))
                .author(DiscordAuthor.getServer().setGameProfile(event.getProfile()))
                .build();

        message.sendMessage();
    }

    @CraterEventListener
    public void userWhitelisted(WhitelistChangedEvent.EntryRemoved event) {
        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.whitelistChanged)
            return;

        DiscordMessage message = new DiscordMessageBuilder(MessageType.WHITELIST)
                .message(SDLinkConfig.INSTANCE.messageFormatting.whitelistRemoved.replace("%player%", event.getProfile().getName()))
                .author(DiscordAuthor.getServer().setGameProfile(event.getProfile()))
                .build();

        message.sendMessage();
    }

    private String parseChatMentions(String input) {
        Pattern pattern = Pattern.compile("([@#])([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(input);

        if (SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
            while (matcher.find()) {
                String type = matcher.group(1);
                String group = matcher.group(2);

                if (type.equals("@")) {
                    Optional<Member> member = CacheManager.getDiscordMembers().stream().filter(m -> m.getEffectiveName().equalsIgnoreCase(group) || m.getUser().getName().equalsIgnoreCase(group)).findFirst();

                    if (member.isPresent()) {
                        input = input.replace(matcher.group(0), member.get().getAsMention());
                    } else {
                        if (CacheManager.getServerRoles().containsKey(matcher.group(0))) {
                            input = input.replace(matcher.group(0), CacheManager.getServerRoles().get(matcher.group(0)));
                        }
                    }
                }

                if (type.equals("#")) {
                    if (CacheManager.getServerChannels().containsKey(matcher.group(0))) {
                        input = input.replace(matcher.group(0), CacheManager.getServerChannels().get(matcher.group(0)));
                    }
                }
            }
        }

        return parseChatEmojis(input);
    }

    private String parseChatEmojis(String input) {
        Pattern pattern = Pattern.compile(":[a-zA-Z0-9_]+:");
        Matcher matcher = pattern.matcher(input);

        if (CacheManager.getCustomEmotes().isEmpty())
            return input;

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String emoji = matcher.group();

            if (CacheManager.getCustomEmotes().containsKey(emoji)) {
                String replacement = CacheManager
                        .getCustomEmotes()
                        .get(emoji)
                        .getAsMention();

                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(result, emoji);
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

}
