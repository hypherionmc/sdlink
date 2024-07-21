/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.managers.EmbedManager;
import com.hypherionmc.sdlink.core.managers.WebhookManager;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.util.SDLinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;

import static net.dv8tion.jda.api.EmbedBuilder.ZERO_WIDTH_SPACE;

/**
 * @author HypherionSA
 * Represents a message sent from Minecraft to Discord
 * This ensures the message is properly formatted and configured
 */
public final class DiscordMessage {

    private final MessageType messageType;
    private final DiscordAuthor author;
    private final String message;
    private final Runnable afterSend;

    /**
     * Private instance. Use {@link DiscordMessageBuilder} to create an instance
     */
    DiscordMessage(DiscordMessageBuilder builder) {
        this.messageType = builder.getMessageType();
        this.author = builder.getAuthor();
        this.message = builder.getMessage();
        this.afterSend = builder.getAfterSend();
    }

    /**
     * Try to send the message to discord
     */
    public void sendMessage() {
        if (!BotController.INSTANCE.isBotReady())
            return;

        if (message.isEmpty())
            return;

        try {
            if (messageType == MessageType.CONSOLE) {
                sendConsoleMessage();
            } else {
                sendNormalMessage();
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                BotController.INSTANCE.getLogger().error("Failed to send Discord Message", e);
            }
        }
    }

    /**
     * Send a Non Console relay message to discord
     */
    private void sendNormalMessage() {
        Triple<GuildMessageChannel, WebhookClient, MessageChannelConfig.DestinationObject> channel = resolveDestination();

        // Check if a webhook is configured, and use that instead
        if (channel.getMiddle() != null && SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.enabled) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.webhookNameFormat.replace("%display_name%", this.author.getDisplayName()).replace("%mc_name%", this.author.getRealPlayerName()));
            if (!this.author.getAvatar().isEmpty()) {
                builder.setAvatarUrl(this.author.getAvatar());
            }

            // Message must be an Embed
            if (channel.getRight().useEmbed) {
                EmbedBuilder eb = buildEmbed(false, channel.getRight().embedLayout);
                WebhookEmbed web = WebhookEmbedBuilder.fromJDA(eb.build()).build();
                builder.addEmbeds(web);
            } else {
                builder.setContent(message);
            }

            channel.getMiddle().send(builder.build()).thenRun(() -> {
                if (afterSend != null)
                    afterSend.run();
            });
        } else {
            if (channel.getLeft() == null) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging)
                    BotController.INSTANCE.getLogger().warn("Expected to get Channel for {}, but got null", messageType.name());
                if (afterSend != null)
                    afterSend.run();
                return;
            }

            // Use the configured channel instead
            if (channel.getRight().useEmbed) {
                EmbedBuilder eb = buildEmbed(true, channel.getRight().embedLayout);
                channel.getLeft().sendMessageEmbeds(eb.build()).queue(success -> {
                    if (afterSend != null)
                        afterSend.run();
                });
            } else {
                channel.getLeft().sendMessage(
                                this.messageType == MessageType.CHAT ?
                                        SDLinkConfig.INSTANCE.messageFormatting.chat.replace("%player%", author.getDisplayName()).replace("%message%", message)
                                        : message)
                        .queue(success -> {
                            if (afterSend != null)
                                afterSend.run();
                        });
            }
        }
    }

    /**
     * Only used for console relay messages
     */
    private void sendConsoleMessage() {
        try {
            if (!BotController.INSTANCE.isBotReady() || !SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages)
                return;

            MessageChannel channel = ChannelManager.getConsoleChannel();
            if (channel != null) {
                channel.sendMessage(this.message).queue();
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                BotController.INSTANCE.getLogger().error("Failed to send console message", e);
            }
        }

        if (afterSend != null)
            afterSend.run();
    }

    /**
     * Build an embed with the supplied information
     *
     * @param withAuthor Should the author be appended to the embed. Not used for Webhooks
     */
    private EmbedBuilder buildEmbed(boolean withAuthor, String key) {
        String embedJson = EmbedManager.getEmbed(key);

        if (embedJson == null || embedJson.isEmpty()) {
            EmbedBuilder builder = new EmbedBuilder();

            if (withAuthor) {
                builder.setAuthor(
                        this.author.getDisplayName(),
                        null,
                        this.author.getAvatar().isEmpty() ? null : this.author.getAvatar()
                );
            }

            builder.setDescription(message);
            return builder;
        }

        embedJson = embedJson
                .replace("%author%", this.author.getDisplayName())
                .replace("%avatar%", this.author.getAvatar())
                .replace("%message_contents%", this.message)
                .replace("%player_avatar%", this.author.getRealPlayerAvatar())
                .replace("%player_name%", this.author.getRealPlayerName())
                .replace("%username%", this.author.getUsername());

        DataObject object = DataObject.fromJson(embedJson);
        return fromData(object);
    }

    /**
     * Figure out where the message must be delivered to, based on the config values
     */
    private Triple<GuildMessageChannel, WebhookClient, MessageChannelConfig.DestinationObject> resolveDestination() {
        switch (messageType) {
            case CHAT -> {
                MessageChannelConfig.DestinationObject chat = SDLinkConfig.INSTANCE.messageDestinations.chat;
                return Triple.of(
                        ChannelManager.getDestinationChannel(chat.channel),
                        WebhookManager.getWebhookClient(chat.channel),
                        chat
                );
            }
            case START -> {
                MessageChannelConfig.DestinationObject startStop = SDLinkConfig.INSTANCE.messageDestinations.start;
                return Triple.of(
                        ChannelManager.getDestinationChannel(startStop.channel),
                        WebhookManager.getWebhookClient(startStop.channel),
                        startStop
                );
            }
            case STOP -> {
                MessageChannelConfig.DestinationObject startStop = SDLinkConfig.INSTANCE.messageDestinations.stop;
                return Triple.of(
                        ChannelManager.getDestinationChannel(startStop.channel),
                        WebhookManager.getWebhookClient(startStop.channel),
                        startStop
                );
            }
            case JOIN -> {
                MessageChannelConfig.DestinationObject joinLeave = SDLinkConfig.INSTANCE.messageDestinations.join;
                return Triple.of(
                        ChannelManager.getDestinationChannel(joinLeave.channel),
                        WebhookManager.getWebhookClient(joinLeave.channel),
                        joinLeave
                );
            }
            case LEAVE -> {
                MessageChannelConfig.DestinationObject joinLeave = SDLinkConfig.INSTANCE.messageDestinations.leave;
                return Triple.of(
                        ChannelManager.getDestinationChannel(joinLeave.channel),
                        WebhookManager.getWebhookClient(joinLeave.channel),
                        joinLeave
                );
            }
            case ADVANCEMENT -> {
                MessageChannelConfig.DestinationObject advancement = SDLinkConfig.INSTANCE.messageDestinations.advancements;
                return Triple.of(
                        ChannelManager.getDestinationChannel(advancement.channel),
                        WebhookManager.getWebhookClient(advancement.channel),
                        advancement
                );
            }
            case DEATH -> {
                MessageChannelConfig.DestinationObject death = SDLinkConfig.INSTANCE.messageDestinations.death;
                return Triple.of(
                        ChannelManager.getDestinationChannel(death.channel),
                        WebhookManager.getWebhookClient(death.channel),
                        death
                );
            }
            case COMMAND -> {
                MessageChannelConfig.DestinationObject command = SDLinkConfig.INSTANCE.messageDestinations.commands;
                return Triple.of(
                        ChannelManager.getDestinationChannel(command.channel),
                        WebhookManager.getWebhookClient(command.channel),
                        command
                );
            }
            case CUSTOM -> {
                MessageChannelConfig.DestinationObject custom = SDLinkConfig.INSTANCE.messageDestinations.custom;
                return Triple.of(
                        ChannelManager.getDestinationChannel(custom.channel),
                        WebhookManager.getWebhookClient(custom.channel),
                        custom
                );
            }
        }

        // This code should never be reached, but it's added here as a fail-safe
        MessageChannelConfig.DestinationObject chat = SDLinkConfig.INSTANCE.messageDestinations.chat;
        return Triple.of(ChannelManager.getDestinationChannel(chat.channel), WebhookManager.getWebhookClient(chat.channel), chat);
    }

    @NotNull
    private EmbedBuilder fromData(@NotNull DataObject data) {
        Checks.notNull(data, "DataObject");
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(data.getString("title", null));
        builder.setUrl(data.getString("url", null));
        builder.setDescription(data.getString("description", ""));

        if (!data.isNull("timestamp") && !data.getString("timestamp").equalsIgnoreCase("0")) {
            builder.setTimestamp(OffsetDateTime.parse(data.getString("timestamp")));
        }

        if (data.getString("color", "#000000").startsWith("#")) {
            builder.setColor(Color.decode(data.getString("color", "#000000")));
        } else {
            builder.setColor(data.getInt("color", Role.DEFAULT_COLOR_RAW));
        }

        data.optObject("thumbnail").ifPresent(thumbnail ->
                builder.setThumbnail(SDLinkUtils.isNullOrEmpty(thumbnail.getString("url")) ? null : thumbnail.getString("url"))
        );

        data.optObject("author").ifPresent(author ->
                builder.setAuthor(
                        author.getString("name", ""),
                        SDLinkUtils.isNullOrEmpty(author.getString("url", null)) ? null : author.getString("url", null),
                        SDLinkUtils.isNullOrEmpty(author.getString("icon_url", null)) ? null : author.getString("icon_url", null)
                )
        );

        data.optObject("footer").ifPresent(footer ->
                builder.setFooter(
                        footer.getString("text", ""),
                        SDLinkUtils.isNullOrEmpty(footer.getString("icon_url", null)) ? null : footer.getString("icon_url", null)
                )
        );

        data.optObject("image").ifPresent(image ->
                builder.setImage(SDLinkUtils.isNullOrEmpty(image.getString("url")) ? null : image.getString("url"))
        );

        data.optArray("fields").ifPresent(arr ->
                arr.stream(DataArray::getObject).forEach(field ->
                        builder.addField(
                                field.getString("name", ZERO_WIDTH_SPACE),
                                field.getString("value", ZERO_WIDTH_SPACE),
                                field.getBoolean("inline", false)
                        )
                )
        );

        return builder;
    }
}
