/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CacheManager {

    @Getter
    private static final HashMap<String, String> serverChannels = new HashMap<>();
    @Getter
    private static final HashMap<String, String> serverRoles = new HashMap<>();
    @Getter
    private static final HashMap<String, String> userCache = new HashMap<>();
    @Getter
    private static final Set<Member> discordMembers = new HashSet<>();

    @Getter
    public static final HashMap<MessageType, MessageChannelConfig.DestinationObject> messageDestinations = new HashMap<>() {{
        put(MessageType.CHAT, SDLinkConfig.INSTANCE.messageDestinations.chat);
        put(MessageType.START, SDLinkConfig.INSTANCE.messageDestinations.start);
        put(MessageType.STOP, SDLinkConfig.INSTANCE.messageDestinations.stop);
        put(MessageType.JOIN, SDLinkConfig.INSTANCE.messageDestinations.join);
        put(MessageType.LEAVE, SDLinkConfig.INSTANCE.messageDestinations.leave);
        put(MessageType.ADVANCEMENTS, SDLinkConfig.INSTANCE.messageDestinations.advancements);
        put(MessageType.DEATH, SDLinkConfig.INSTANCE.messageDestinations.death);
        put(MessageType.COMMANDS, SDLinkConfig.INSTANCE.messageDestinations.commands);
        put(MessageType.CUSTOM, SDLinkConfig.INSTANCE.messageDestinations.custom);
    }};

    public static void loadCache() {
        loadChannelCache();
        loadRoleCache();
        loadUserCache();
    }

    public static void loadChannelCache() {
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

}
