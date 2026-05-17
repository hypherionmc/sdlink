package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import com.hypherionmc.sdlinkrw.util.Debugger;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.List;

@Deprecated(forRemoval = true)
public final class DestinationHolder {

    private final List<GuildMessageChannel> channel;
    private final MessageChannelConfig.DestinationObject destination;

    private DestinationHolder(MessageChannelConfig.DestinationObject destination, MessageType type) {
        // TODO Handle Overrides
        this.channel = SDLCache.INSTANCE.getChannelDestinations(destination.channel);

        Debugger.INSTANCE.log("Destination has " + channel.size() + " channels");

        this.destination = destination;
    }

    public static DestinationHolder of(MessageChannelConfig.DestinationObject destination, MessageType type) {
        return new DestinationHolder(destination, type);
    }

    public List<GuildMessageChannel> channel() {
        return channel;
    }

    public MessageDestination destination() {
        return destination.channel;
    }

    public boolean useEmbed() {
        return destination.useEmbed;
    }

    public String embedLayout() {
        return destination.embedLayout;
    }
}
