package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.api.event.common.CraterLivingDeathEvent;
import com.hypherionmc.craterlib.api.event.server.*;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.events.SDLinkReadyEvent;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.core.util.LogReader;
import com.hypherionmc.sdlink.networking.MentionsSyncPacket;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.commands.DiscordCommand;
import com.hypherionmc.sdlink.server.commands.ReloadEmbedsCommand;
import com.hypherionmc.sdlink.server.commands.WhoisCommand;
import com.hypherionmc.sdlink.util.ModUtils;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.world.entity.player.Player;

public class ServerEvents {

    private MinecraftServer minecraftServer;
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
        DiscordCommand.register(event.getDispatcher());
        ReloadEmbedsCommand.register(event.getDispatcher());
        WhoisCommand.register(event.getDispatcher());
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
            BotController.INSTANCE.checkWhiteListing();

            DiscordMessage message = new DiscordMessageBuilder(MessageType.START)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStarted)
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
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
                    .afterSend(() -> BotController.INSTANCE.shutdownBot(true))
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerChatEvent(CraterServerChatEvent event) {
        if (!SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer())) {
            return;
        }

        onServerChatEvent(event.getComponent(), event.getPlayer().getDisplayName(), SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(event.getPlayer()), event.getPlayer().getGameProfile(), false);
    }

    public void onServerChatEvent(Component message, Component user, String uuid, GameProfile gameProfile, boolean fromServer) {
        if (user == null || message == null)
            return;

        if (!canSendMessage())
            return;

        try {
            if (SDLinkConfig.INSTANCE.chatConfig.playerMessages) {
                String username = ModUtils.resolve(user);
                String msg = ModUtils.resolve(message);

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

        String cmd = event.getParseResults().getReader().getString();

        ServerPlayer player = null;
        String uuid = null;
        Component user = Component.literal("Unknown");
        GameProfile profile = null;
        try {
            player = event.getParseResults().getContext().getLastChild().getSource().getPlayerOrException();
            uuid = SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(player);
            user = player.getDisplayName();
            profile = player.getGameProfile();
        } catch (CommandSyntaxException ignored) {}

        if (player != null && !SDLinkMCPlatform.INSTANCE.playerIsActive(player))
            return;

        String command = cmd.startsWith("/") ? cmd.replaceFirst("/", "") : cmd;
        String cmdName = command.split(" ")[0];
        String username = ModUtils.resolve(user);

        if (username == null || username.equalsIgnoreCase("unknown")) {
            username = "Server";
        }

        if ((cmdName.startsWith("say") || cmdName.startsWith("me")) && SDLinkConfig.INSTANCE.chatConfig.sendSayCommand) {
            String msg = ModUtils.strip(command, "say", "me");
            msg = ModUtils.resolve(Component.literal(msg));

            DiscordAuthor author = DiscordAuthor.of(
                    username,
                    uuid == null ? "" : uuid,
                    profile != null ? profile.getName() : (player != null ? player.getName().getString() : "server")
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
            CommandContext<CommandSourceStack> context = event.getParseResults().getContext().build(event.getParseResults().getReader().getString());
            StringRange selector_range = event.getParseResults().getContext().getArguments().get("targets").getRange();
            String target = context.getInput().substring(selector_range.getStart(), selector_range.getEnd());

            if (!target.equals("@a"))
                return;

            DiscordAuthor author = DiscordAuthor.of(username, uuid == null ? "" : uuid, profile != null ? profile.getName() : player.getName().getString());

            if (profile != null)
                author.setGameProfile(profile);

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(author)
                    .message(ModUtils.resolve(ComponentArgument.getComponent(context, "message")))
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
                SDLinkNetworking.networkHandler.sendTo(packet, event.getPlayer());
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to sync Mentions to Client", e);
            }
        }

        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.playerJoin || !SDLinkMCPlatform.INSTANCE.playerIsActive(event.getPlayer()))
            return;

        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.JOIN)
                .message(SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", ModUtils.resolve(event.getPlayer().getDisplayName())))
                .author(DiscordAuthor.SERVER)
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
                    UserBanList list = this.minecraftServer.getPlayerList().getBans();
                    if (list.isBanned(event.getPlayer().getGameProfile())) {
                        account.banDiscordMember();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerLeave) {
            String name = ModUtils.resolve(event.getPlayer().getDisplayName());

            DiscordMessage message = new DiscordMessageBuilder(MessageType.LEAVE)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", name))
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onPlayerDeath(CraterLivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !SDLinkMCPlatform.INSTANCE.playerIsActive(p))
            return;

        if (event.getEntity() instanceof Player player) {
            if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.deathMessages) {

                String name = ModUtils.resolve(player.getDisplayName());
                String msg = ModUtils.resolve(event.getDamageSource().getLocalizedDeathMessage(player));

                if (msg.startsWith(name + " ")) {
                    msg = msg.substring((name + " ").length());
                }

                DiscordMessage message = new DiscordMessageBuilder(MessageType.DEATH)
                        .message(SDLinkConfig.INSTANCE.messageFormatting.death.replace("%player%", name).replace("%message%", msg))
                        .author(DiscordAuthor.SERVER)
                        .build();

                message.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void onPlayerAdvancement(CraterAdvancementEvent event) {
        if (!SDLinkMCPlatform.INSTANCE.playerIsActive((ServerPlayer) event.getPlayer()))
            return;

        try {
            if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.advancementMessages) {
                String username = ModUtils.resolve(event.getPlayer().getDisplayName());
                String finalAdvancement = ModUtils.resolve(event.getAdvancement().display().get().getTitle());
                String advancementBody = ModUtils.resolve(event.getAdvancement().display().get().getDescription());

                DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.ADVANCEMENT)
                        .message(SDLinkConfig.INSTANCE.messageFormatting.achievements.replace("%player%", username).replace("%title%", finalAdvancement).replace("%description%", advancementBody))
                        .author(DiscordAuthor.SERVER)
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
                    UserBanList list = this.minecraftServer.getPlayerList().getBans();
                    if (list.isBanned(event.getGameProfile())) {
                        account.banDiscordMember();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            var result = account.canLogin();

            if (result.isError())
                event.setMessage(Component.literal(result.getMessage()));
        }
    }

    @CraterEventListener
    public void sdlinkReadyEvent(SDLinkReadyEvent event) {
        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages)
            LogReader.init(ModloaderEnvironment.INSTANCE.isDevEnv());
    }

    public boolean canSendMessage() {
        return BotController.INSTANCE != null && BotController.INSTANCE.isBotReady() && SDLinkConfig.INSTANCE != null;
    }

    public MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    public long getUptime() {
        return uptime;
    }
}
