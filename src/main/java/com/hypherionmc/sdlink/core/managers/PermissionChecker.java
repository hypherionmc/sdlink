/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HypherionSA
 * Startup permission checker hook to check if the bot has all the required permissions to function
 */
public class PermissionChecker {

    // Invite URL for bot shown in server logs
    private static final String DISCORD_INVITE = "https://discord.com/api/oauth2/authorize?client_id={bot_id}&permissions=277831183380&scope=bot%20applications.commands";

    // Base Permissions required by the bot to operate
    private static final List<Permission> BOT_PERMS = new ArrayList<>() {{
        add(Permission.NICKNAME_CHANGE);
        add(Permission.MANAGE_WEBHOOKS);
        add(Permission.MESSAGE_SEND);
        add(Permission.MESSAGE_EMBED_LINKS);
        add(Permission.MESSAGE_HISTORY);
        add(Permission.MESSAGE_EXT_EMOJI);
        add(Permission.MANAGE_ROLES);
        add(Permission.MESSAGE_MANAGE);
        add(Permission.MESSAGE_SEND_IN_THREADS);
    }};

    // Basic channel permissions required by all channels
    private static final List<Permission> BASE_CHANNEL_PERMS = new ArrayList<>() {{
        add(Permission.VIEW_CHANNEL);
        add(Permission.MESSAGE_SEND);
        add(Permission.MESSAGE_EMBED_LINKS);
        add(Permission.MANAGE_WEBHOOKS);
    }};

    /**
     * Run the permission checker to see if the bot has all the required permissions
     */
    public static void checkBotSetup() {
        if (SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan) {
            BOT_PERMS.add(Permission.BAN_MEMBERS);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\r\n").append("******************* Simple Discord Link Errors *******************").append("\r\n");
        AtomicInteger errCount = new AtomicInteger();
        BotController controller = BotController.INSTANCE;

        if (!controller.isBotReady())
            return;

        controller.getLogger().info("Discord Invite Link for Bot: {}", DISCORD_INVITE.replace("{bot_id}", controller.getJDA().getSelfUser().getId()));

        if (controller.getJDA().getGuilds().isEmpty()) {
            errCount.incrementAndGet();
            builder.append(errCount.get())
                    .append(") ")
                    .append("Bot does not appear to be in any servers. You need to invite the bot to your discord server before chat relays will work. Use link ")
                    .append(DISCORD_INVITE.replace("{bot_id}", controller.getJDA().getSelfUser().getId())).append(" to invite the bot.")
                    .append("\r\n");
        } else {
            if (controller.getJDA().getGuilds().size() > 1) {
                errCount.incrementAndGet();
                builder.append(errCount.get())
                        .append(") ")
                        .append("Bot appears to be in multiple discord servers. This mod is only designed to work with a single discord server")
                        .append("\r\n");
            } else {
                Guild guild = controller.getJDA().getGuilds().get(0);

                if (guild != null) {
                    Member bot = guild.getMemberById(controller.getJDA().getSelfUser().getIdLong());
                    EnumSet<Permission> botPerms = bot.getPermissionsExplicit();

                    RoleManager.loadRequiredRoles(errCount, builder);

                    if (!botPerms.contains(Permission.ADMINISTRATOR)) {
                        checkBotPerms(errCount, builder, botPerms);

                        checkChannelPerms(
                                SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID,
                                "Chat Channel",
                                errCount,
                                builder,
                                bot,
                                true,
                                true
                        );

                        checkChannelPerms(
                                SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.eventsChannelID,
                                "Events Channel",
                                errCount,
                                builder,
                                bot,
                                false,
                                false
                        );

                        checkChannelPerms(
                                SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.consoleChannelID,
                                "Console Channel",
                                errCount,
                                builder,
                                bot,
                                false,
                                false
                        );
                    }
                }
            }
        }

        if (errCount.get() > 0) {
            builder.append("\r\n").append("******************* Simple Discord Link Errors *******************").append("\r\n");
            controller.getLogger().error(builder.toString());
        }
    }

    private static void checkBotPerms(AtomicInteger errCount, StringBuilder builder, EnumSet<Permission> permissions) {
        BOT_PERMS.forEach(perm -> {
            if (!permissions.contains(perm)) {
                errCount.incrementAndGet();
                builder.append(errCount.get())
                        .append(") ")
                        .append("Missing Bot Permission: ")
                        .append(perm.getName())
                        .append("\r\n");
            }
        });
    }

    private static void checkChannelPerms(String channelID, String channelName, AtomicInteger errCount, StringBuilder builder, Member bot, boolean channelRequired, boolean isChat) {
        if (channelRequired && channelID.equalsIgnoreCase("0")) {
            errCount.incrementAndGet();
            builder.append(errCount.get())
                    .append(") ")
                    .append(channelName)
                    .append(" ID is not set.... This value is required")
                    .append("\r\n");
            return;
        }

        if (channelID.equalsIgnoreCase("0")) {
            BotController.INSTANCE.getLogger().warn("Channel ID for {} is set to 0. Falling back to chatChannel", channelName);
            return;
        }

        GuildMessageChannel channel = BotController.INSTANCE.getJDA().getChannelById(GuildMessageChannel.class, channelID);

        if (channel == null) {
            errCount.incrementAndGet();
            builder.append(errCount.get())
                    .append(") ")
                    .append(channelName)
                    .append(" ID does not point to a valid Discord Channel. Please double check this")
                    .append("\r\n");
        } else {
            EnumSet<Permission> chatPerms = bot.getPermissions(channel);

            BASE_CHANNEL_PERMS.forEach(perm -> {
                if (!chatPerms.contains(perm)) {
                    errCount.incrementAndGet();
                    builder.append(errCount.get())
                            .append(") ")
                            .append("Missing ")
                            .append(channelName)
                            .append(" Channel Permission: ")
                            .append(perm.getName())
                            .append("\r\n");
                }
            });

            if (isChat) {
                if (SDLinkConfig.INSTANCE.botConfig.channelTopic.doTopicUpdates && !chatPerms.contains(Permission.MANAGE_CHANNEL)) {
                    errCount.incrementAndGet();
                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Manage Channel. Topic updates will not work").append("\r\n");
                }
            }
        }
    }
}
