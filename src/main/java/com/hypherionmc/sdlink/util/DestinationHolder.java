package com.hypherionmc.sdlink.util;

import club.minnced.discord.webhook.WebhookClient;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.managers.WebhookManager;
import com.hypherionmc.sdlink.core.messaging.MessageDestination;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.Nullable;

public class DestinationHolder {

    private final GuildMessageChannel channel;
    private final WebhookClient webhookClient;
    private final MessageChannelConfig.DestinationObject destination;

    protected DestinationHolder(MessageChannelConfig.DestinationObject destination, MessageType type) {

        if (ChannelManager.getOverride(type) != null) {
            this.channel = ChannelManager.getOverride(type);
        } else {
            this.channel = ChannelManager.getDestinationChannel(destination.channel);
        }

        if (WebhookManager.getOverride(type) != null) {
            this.webhookClient = WebhookManager.getOverride(type);
        } else {
            this.webhookClient = WebhookManager.getWebhookClient(destination.channel);
        }

        this.destination = destination;
    }

    public static DestinationHolder of(MessageChannelConfig.DestinationObject destination, MessageType type) {
        return new DestinationHolder(destination, type);
    }

    @Nullable
    public GuildMessageChannel channel() {
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
