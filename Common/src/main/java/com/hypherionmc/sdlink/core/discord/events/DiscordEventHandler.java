/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.events;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.sdlink.compat.rolesync.RoleSync;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.commands.slash.general.ServerStatusSlashCommand;
import com.hypherionmc.sdlink.core.discord.hooks.BotReadyHooks;
import com.hypherionmc.sdlink.core.discord.hooks.DiscordMessageHooks;
import com.hypherionmc.sdlink.core.discord.hooks.DiscordRoleHooks;
import com.hypherionmc.sdlink.core.discord.hooks.MinecraftCommandHook;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.server.SDLinkMinecraftBridge;
import com.hypherionmc.sdlink.util.PKUtil;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.api.events.SDLinkReadyEvent;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateRolesEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.CloseCode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author HypherionSA
 * Class to provide Hooks for Discord Events, such as message received, and login
 * NOTE TO DEVELOPERS: Don't add ANY LOGIC IN HERE. Rather implement it in a seperate class,
 * and use these hooks to trigger that code
 */
public final class DiscordEventHandler extends ListenerAdapter {

    private boolean isStuckInNotReady = false;

    /**
     * Discord yeeted the bot connection
     */
    @Override
    public void onShutdown(ShutdownEvent event) {
        CloseCode code = event.getCloseCode();

        if (code == null) {
            SDLinkConstants.LOGGER.error("Got disconnected from discord for an unknown reason. Code: {}", event.getCode());
            return;
        }

        if (code == CloseCode.DISALLOWED_INTENTS) {
            SDLinkConstants.LOGGER.error("Your bot is missing a required setup step, and cannot continue. Please review https://sdlink.fdd-docs.com/installation/bot-creation/#privileged-gateway-intents to fix this");
            return;
        }

        SDLinkConstants.LOGGER.error("Disconnected from discord with error {}", event.getCloseCode().name());
    }

    /**
     * The bot received a message
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor() == event.getJDA().getSelfUser())
            return;

        if (event.isFromType(ChannelType.PRIVATE)) {
            DiscordMessageHooks.checkVerification(event);
            return;
        }

        if (!event.isFromGuild())
            return;

        if (!event.isWebhookMessage()) {
            MinecraftCommandHook.discordMessageEvent(event);
        }

        if (SDLinkConfig.INSTANCE.channels.chatMessages.pluralKitCompat && PKUtil.PK_USERS.contains(event.getAuthor().getId())) {
            new Thread(() -> {
                try {
                    Thread.sleep(SDLinkConfig.INSTANCE.channels.chatMessages.pluralKitCompatMessageDelay);
                } catch (InterruptedException e) {
                    SDLinkConstants.LOGGER.error("Unexpected InterruptedException", e);
                }

                DiscordMessageHooks.discordMessageEvent(event);
            }).start();
        } else
            DiscordMessageHooks.discordMessageEvent(event);

    }

    /**
     * The bot is connected to discord and ready to begin sending messages
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (event.getJDA().getStatus() != JDA.Status.CONNECTED && event.getJDA().getStatus() != JDA.Status.DISCONNECTED) {
            isStuckInNotReady = true;
            startReadyDetection(event.getJDA());
        }

        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            isStuckInNotReady = false;
            SDLinkConstants.LOGGER.info("Successfully connected to discord");

            SDLCache.INSTANCE.loadCache(event.getJDA());
            SDLCache.INSTANCE.checkBotSetup();
            BotReadyHooks.startActivityUpdates(event);
            BotReadyHooks.startTopicUpdates();
            CraterEventBus.INSTANCE.postEvent(new SDLinkReadyEvent());
        }
    }

    /**
     * A button was clicked.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("sdrefreshbtn")) {
            event.deferEdit().queue(s -> s.editOriginalEmbeds(ServerStatusSlashCommand.runStatusCommand()).queue());
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadMemberCache(event.getGuild());
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadMemberCache(event.getGuild());
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadMemberCache(event.getGuild());
        }

        if (event.getUser().isBot() || !(SDLinkConfig.INSTANCE.accessControl.enabled || SDLinkConfig.INSTANCE.accessControl.optionalVerification))
            return;

        try {
            List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class);
            Optional<SDLinkAccount> account = accounts.stream().filter(a -> a.getDiscordID() != null && a.getDiscordID().equalsIgnoreCase(event.getUser().getId())).findFirst();
            account.ifPresent(a -> DatabaseManager.INSTANCE.deleteEntry(a, SDLinkAccount.class));
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to remove linked account", e);
        }
    }

    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadRoleCache(event.getGuild());
        }
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadRoleCache(event.getGuild());
        }
    }

    @Override
    public void onGenericRoleUpdate(GenericRoleUpdateEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadRoleCache(event.getGuild());
        }
    }

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadChannelCache(event.getGuild());
        }
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            SDLCache.INSTANCE.reloadChannelCache(event.getGuild());
        }
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        if (event.getUser().isBot())
            return;

        SDLCache.INSTANCE.reloadMemberCache(event.getGuild());

        if (!SDLinkConfig.INSTANCE.accessControl.enabled && !SDLinkConfig.INSTANCE.accessControl.optionalVerification)
            return;

        try {
            List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class);
            Optional<SDLinkAccount> account = accounts.stream().filter(a -> a.getDiscordID() != null && a.getDiscordID().equalsIgnoreCase(event.getUser().getId())).findFirst();

            account.ifPresent(a -> {
                MinecraftAccount acc = MinecraftAccount.of(a);

                if (acc != null) {
                    if (SDLinkConfig.INSTANCE.accessControl.banPlayerOnDiscordBan) {
                        SDLinkMinecraftBridge.INSTANCE.banPlayer(acc);
                    }
                }

                DatabaseManager.INSTANCE.deleteEntry(a, SDLinkAccount.class);
            });
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to remove linked account", e);
        }
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        DiscordRoleHooks.INSTANCE.onRoleAdded(event);

        event.getRoles().forEach(role -> {
            RoleSync.INSTANCE.roleAddedToMember(event.getMember(), role, event.getGuild());
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        DiscordRoleHooks.INSTANCE.onRoleRemoved(event);

        event.getRoles().forEach(role -> {
            RoleSync.INSTANCE.roleRemovedFromMember(event.getMember(), role, event.getGuild(), null);
        });
    }

    @Override
    public void onEmojiAdded(EmojiAddedEvent event) {
        SDLCache.INSTANCE.reloadEmojiCache(event.getGuild());
    }

    @Override
    public void onEmojiRemoved(EmojiRemovedEvent event) {
        SDLCache.INSTANCE.reloadEmojiCache(event.getGuild());
    }

    @Override
    public void onEmojiUpdateName(EmojiUpdateNameEvent event) {
        SDLCache.INSTANCE.reloadEmojiCache(event.getGuild());
    }

    @Override
    public void onEmojiUpdateRoles(EmojiUpdateRolesEvent event) {
        SDLCache.INSTANCE.reloadEmojiCache(event.getGuild());
    }

    private void startReadyDetection(JDA jda) {
        BotController.INSTANCE.updatesManager.scheduleAtFixedRate(() -> {
            if (isStuckInNotReady && jda.getStatus() == JDA.Status.CONNECTED) {
                onReady(new ReadyEvent(jda));
                isStuckInNotReady = false;
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}
