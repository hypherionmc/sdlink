/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.messaging.MessageDestination;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HypherionSA
 * Load and Cache configured channels for later use
 */
public class ChannelManager {

    private static final HashMap<MessageDestination, GuildMessageChannel> channelMap = new HashMap<>();
    private static final HashMap<MessageType, GuildMessageChannel> overrideChannels = new HashMap<>();

    @Getter
    private static GuildMessageChannel consoleChannel;

    /**
     * Load configured channel, while always defaulting back to ChatChannel for channels that aren't configured
     */
    public static void loadChannels() {
        channelMap.clear();
        overrideChannels.clear();

        JDA jda = BotController.INSTANCE.getJDA();

        GuildMessageChannel chatChannel = jda.getChannelById(GuildMessageChannel.class, SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID);
        GuildMessageChannel eventChannel = jda.getChannelById(GuildMessageChannel.class, SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.eventsChannelID);
        consoleChannel = jda.getChannelById(GuildMessageChannel.class, SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.consoleChannelID);

        if (chatChannel != null) {
            channelMap.put(MessageDestination.CHAT, chatChannel);
        }

        channelMap.put(MessageDestination.EVENT, eventChannel != null ? eventChannel : chatChannel);
        channelMap.put(MessageDestination.CONSOLE, consoleChannel != null ? consoleChannel : chatChannel);

        for (Map.Entry<MessageType, MessageChannelConfig.DestinationObject> d : CacheManager.messageDestinations.entrySet()) {
            String override = EncryptionUtil.INSTANCE.decrypt(d.getValue().override);
            if (!d.getValue().channel.isOverride() || d.getValue().override == null || override.startsWith("http"))
                continue;

            String id = d.getValue().override;
            if (overrideChannels.containsKey(d.getKey()))
                continue;

            GuildMessageChannel channel = jda.getChannelById(GuildMessageChannel.class, id);
            if (channel == null) {
                BotController.INSTANCE.getLogger().error("Failed to load override channel {} for {}", id, d.getKey().name());
                continue;
            }

            BotController.INSTANCE.getLogger().info("Using channel override {} for {}", channel.getName(), d.getKey().name());
            overrideChannels.put(d.getKey(), channel);
        }
    }

    @Nullable
    public static GuildMessageChannel getOverride(MessageType type) {
        if (overrideChannels.get(type) == null)
            return null;

        return overrideChannels.get(type);
    }

    @Nullable
    public static GuildMessageChannel getDestinationChannel(MessageDestination destination) {
        if (channelMap.get(destination) == null)
            return null;

        return channelMap.get(destination);
    }
}
