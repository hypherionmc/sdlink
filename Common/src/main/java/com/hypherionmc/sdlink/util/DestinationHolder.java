package com.hypherionmc.sdlink.util;

import club.minnced.discord.webhook.WebhookClient;
import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DestinationHolder {

    private final List<GuildMessageChannel> channel;
    private final WebhookClient webhookClient = null;
    private final MessageChannelConfig.DestinationObject destination;

    private DestinationHolder(MessageChannelConfig.DestinationObject destination, MessageType type) {

        if (ChannelManager.getOverride(type) != null) {
            this.channel = new ArrayList<>();
        } else {
            this.channel = SDLCache.INSTANCE.getChannelDestinations(destination.channel);
        }

        Debugger.INSTANCE.log("Destination has " + channel.size() + " channels");

        this.destination = destination;
    }

    public static DestinationHolder of(MessageChannelConfig.DestinationObject destination, MessageType type) {
        return new DestinationHolder(destination, type);
    }

    public List<GuildMessageChannel> channel() {
        return channel;
    }

    @Nullable
    public WebhookClient webhook() {
        return webhookClient;
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

    public boolean hasWebhook() {
        return this.webhookClient != null;
    }
}
