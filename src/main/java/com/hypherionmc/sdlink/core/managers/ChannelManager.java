/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.messaging.MessageDestination;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author HypherionSA
 * Load and Cache configured channels for later use
 */
public class ChannelManager {

    private static final HashMap<MessageDestination, Pair<GuildMessageChannel, Boolean>> channelMap = new HashMap<>();
    @Getter
    private static GuildMessageChannel consoleChannel;

    /**
     * Load configured channel, while always defaulting back to ChatChannel for channels that aren't configured
     */
    public static void loadChannels() {
        channelMap.clear();

        JDA jda = BotController.INSTANCE.getJDA();

        GuildMessageChannel chatChannel = jda.getChannelById(GuildMessageChannel.class, SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID);
        GuildMessageChannel eventChannel = jda.getChannelById(GuildMessageChannel.class, SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.eventsChannelID);
        consoleChannel = jda.getChannelById(GuildMessageChannel.class, SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.consoleChannelID);

        if (chatChannel != null) {
            channelMap.put(MessageDestination.CHAT, Pair.of(chatChannel, false));
        }

        channelMap.put(MessageDestination.EVENT, eventChannel != null ? Pair.of(eventChannel, false) : Pair.of(chatChannel, false));
        channelMap.put(MessageDestination.CONSOLE, consoleChannel != null ? Pair.of(consoleChannel, true) : Pair.of(chatChannel, false));
    }

    @Nullable
    public static GuildMessageChannel getDestinationChannel(MessageDestination destination) {
        if (channelMap.get(destination) == null)
            return null;

        return channelMap.get(destination).getLeft();
    }
}
