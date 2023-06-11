package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.api.event.common.CraterLivingDeathEvent;
import com.hypherionmc.craterlib.api.event.server.*;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.events.SDLinkReadyEvent;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.core.util.LogReader;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.commands.DiscordCommand;
import com.hypherionmc.sdlink.server.commands.ReloadModCommand;
import com.hypherionmc.sdlink.server.commands.WhoisCommand;
import com.hypherionmc.sdlink.util.ModUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

    public static void reloadInstance(MinecraftServer server) {
        if (events != null)
            BotController.INSTANCE.shutdownBot();

        events = new ServerEvents();
        events.minecraftServer = server;
    }

    private ServerEvents() {
        BotController.newInstance(SDLinkConstants.LOGGER);
        BotController.INSTANCE.initializeBot();
    }

    @CraterEventListener
    public void onCommandRegister(CraterRegisterCommandEvent event) {
        DiscordCommand.register(event.getDispatcher());
        ReloadModCommand.register(event.getDispatcher());
        WhoisCommand.register(event.getDispatcher());
    }

    @CraterEventListener
    public void onServerStarting(CraterServerLifecycleEvent.Starting event) {
        this.minecraftServer = event.getServer();
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStarting) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.START_STOP)
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

            DiscordMessage message = new DiscordMessageBuilder(MessageType.START_STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStarted)
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerStopping(CraterServerLifecycleEvent.Stopping event) {
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStopping) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.START_STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStopping)
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerStoppedEvent(CraterServerLifecycleEvent.Stopped event) {
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.serverStopped) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.START_STOP)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.serverStopped)
                    .author(DiscordAuthor.SERVER)
                    .afterSend(() -> BotController.INSTANCE.shutdownBot(true))
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onServerChatEvent(CraterServerChatEvent event) {
        onServerChatEvent(event.getComponent(), event.getPlayer().getDisplayName(), SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(event.getPlayer()), false);
    }

    public void onServerChatEvent(Component message, Component user, String uuid, boolean fromServer) {
        if (user == null || message == null)
            return;

        if (!canSendMessage())
            return;

        try {
            if (SDLinkConfig.INSTANCE.chatConfig.playerMessages) {
                String username = ModUtils.resolve(user);
                String msg = ModUtils.resolve(message);

                DiscordAuthor author = DiscordAuthor.of(username, uuid);
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
        String uuid = null;
        Component user = Component.literal("Unknown");
        try {
            ServerPlayer player = event.getParseResults().getContext().getLastChild().getSource().getPlayerOrException();
            uuid = SDLinkMCPlatform.INSTANCE.getPlayerSkinUUID(player);
            user = player.getDisplayName();
        } catch (CommandSyntaxException ignored) {}


        String command = cmd.startsWith("/") ? cmd.replaceFirst("/", "") : cmd;
        String cmdName = command.split(" ")[0];
        String username = ModUtils.resolve(user);

        if (username == null || username.equalsIgnoreCase("unknown")) {
            username = "Server";
        }

        if ((cmdName.startsWith("say") || cmdName.startsWith("me")) && SDLinkConfig.INSTANCE.chatConfig.sendSayCommand) {
            String msg = ModUtils.strip(command, "say", "me");
            msg = ModUtils.resolve(Component.literal(msg));

            DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CHAT)
                    .author(DiscordAuthor.of(username, uuid == null ? "" : uuid))
                    .message(msg)
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
        if (!canSendMessage() || !SDLinkConfig.INSTANCE.chatConfig.playerJoin)
            return;

        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.JOIN_LEAVE)
                .message(SDLinkConfig.INSTANCE.messageFormatting.playerJoined.replace("%player%", ModUtils.resolve(event.getPlayer().getDisplayName())))
                .author(DiscordAuthor.SERVER)
                .build();

        discordMessage.sendMessage();
    }

    @CraterEventListener
    public void playerLeaveEvent(CraterPlayerEvent.PlayerLoggedOut event) {
        if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.playerLeave) {

            String name = ModUtils.resolve(event.getPlayer().getDisplayName());

            DiscordMessage message = new DiscordMessageBuilder(MessageType.JOIN_LEAVE)
                    .message(SDLinkConfig.INSTANCE.messageFormatting.playerLeft.replace("%player%", name))
                    .author(DiscordAuthor.SERVER)
                    .build();

            message.sendMessage();
        }
    }

    @CraterEventListener
    public void onPlayerDeath(CraterLivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.deathMessages) {

                String name = ModUtils.resolve(player.getDisplayName());
                String msg = ModUtils.resolve(event.getDamageSource().getLocalizedDeathMessage(player));

                DiscordMessage message = new DiscordMessageBuilder(MessageType.DEATH)
                        .message(msg)
                        .author(DiscordAuthor.SERVER)
                        .build();

                message.sendMessage();
            }
        }
    }

    @CraterEventListener
    public void onPlayerAdvancement(CraterAdvancementEvent event) {
        try {
            if (canSendMessage() && SDLinkConfig.INSTANCE.chatConfig.advancementMessages) {
                String username = ModUtils.resolve(event.getPlayer().getDisplayName());
                String finalAdvancement = ModUtils.resolve(event.getAdvancement().getDisplay().getTitle());
                String advancementBody = ModUtils.resolve(event.getAdvancement().getDisplay().getDescription());

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

        if (SDLinkConfig.INSTANCE != null && SDLinkConfig.INSTANCE.whitelistingAndLinking.accountLinking.accountLinking) {
            MinecraftAccount account = MinecraftAccount.standard(event.getGameProfile().getName());
            SDLinkAccount savedAccount = account.getStoredAccount();

            if (savedAccount == null) {
                event.setMessage(Component.literal("This server requires you to link your Discord and Minecraft account. Please contact the owner for more info"));
                return;
            }

            if (!account.isAccountLinked() && savedAccount.getAccountLinkCode() != null && !savedAccount.getAccountLinkCode().isEmpty()) {
                event.setMessage(Component.literal("Account Link Code: " + savedAccount.getAccountLinkCode()));
            }
        }

        if (SDLinkConfig.INSTANCE != null && SDLinkConfig.INSTANCE.whitelistingAndLinking.whitelisting.whitelisting) {
            MinecraftAccount account = MinecraftAccount.standard(event.getGameProfile().getName());
            SDLinkAccount savedAccount = account.getStoredAccount();

            if (savedAccount == null)
                return;

            if (!account.isAccountWhitelisted() && savedAccount.getWhitelistCode() != null && !savedAccount.getWhitelistCode().isEmpty()) {
                event.setMessage(Component.literal("Account Whitelist Code: " + savedAccount.getWhitelistCode()));
            }
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
