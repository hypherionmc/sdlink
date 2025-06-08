/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class CacheManager {

    @Getter
    private static final HashMap<String, String> serverChannels = new HashMap<>();
    @Getter
    private static final HashMap<String, String> serverRoles = new HashMap<>();
    @Getter
    private static final HashMap<String, String> userCache = new HashMap<>();
    @Getter
    private static final Set<Member> discordMembers = new HashSet<>();
    @Getter
    private static final HashMap<String, RichCustomEmoji> customEmotes = new HashMap<>();

    @Getter
    public static final HashMap<MessageType, MessageChannelConfig.DestinationObject> messageDestinations = new HashMap<>();

    public static void loadCache() {
        loadChannelCache();
        loadRoleCache();
        loadUserCache();
        loadEmoteCache();
    }

    public static void loadChannelCache() {
        reloadChannelConfigCache();
        serverChannels.clear();

        JDA jda = BotController.INSTANCE.getJDA();

        if (jda.getGuilds().isEmpty())
            return;

        jda.getGuilds().get(0).getChannels(false).forEach(c -> {
            if (c.getType() != ChannelType.CATEGORY) {
                serverChannels.put("#" + c.getName(), c.getAsMention());
            }
        });
    }

    public static void loadRoleCache() {
        serverRoles.clear();

        JDA jda = BotController.INSTANCE.getJDA();

        if (jda.getGuilds().isEmpty())
            return;

        jda.getGuilds().get(0).getRoles().forEach(r -> {
            if (r.isMentionable() && !r.isManaged()) {
                serverRoles.put("@" + r.getName(), r.getAsMention());
            }
        });
    }

    public static void loadUserCache() {
        userCache.clear();
        discordMembers.clear();

        JDA jda = BotController.INSTANCE.getJDA();

        if (jda.getGuilds().isEmpty())
            return;

        jda.getGuilds().get(0).getMembers().forEach(r -> {
            userCache.put("@" + r.getEffectiveName(), r.getAsMention());
            discordMembers.add(r);
        });
    }

    public static void loadEmoteCache() {
        customEmotes.clear();

        JDA jda = BotController.INSTANCE.getJDA();

        if (jda.getGuilds().isEmpty())
            return;

        jda.getGuilds().get(0).getEmojis().forEach(e -> customEmotes.put(":" + e.getName() + ":", e));
    }

    public static void reloadChannelConfigCache() {
        try {
            messageDestinations.clear();
            messageDestinations.put(MessageType.CHAT, SDLinkConfig.INSTANCE.messageDestinations.chat);
            messageDestinations.put(MessageType.START, SDLinkConfig.INSTANCE.messageDestinations.start);
            messageDestinations.put(MessageType.STOP, SDLinkConfig.INSTANCE.messageDestinations.stop);
            messageDestinations.put(MessageType.JOIN, SDLinkConfig.INSTANCE.messageDestinations.join);
            messageDestinations.put(MessageType.LEAVE, SDLinkConfig.INSTANCE.messageDestinations.leave);
            messageDestinations.put(MessageType.ADVANCEMENTS, SDLinkConfig.INSTANCE.messageDestinations.advancements);
            messageDestinations.put(MessageType.DEATH, SDLinkConfig.INSTANCE.messageDestinations.death);
            messageDestinations.put(MessageType.COMMANDS, SDLinkConfig.INSTANCE.messageDestinations.commands);
            messageDestinations.put(MessageType.WHITELIST, SDLinkConfig.INSTANCE.messageDestinations.whitelist);
            messageDestinations.put(MessageType.CUSTOM, SDLinkConfig.INSTANCE.messageDestinations.custom);
        } catch (Exception ignored) {}
    }

}
