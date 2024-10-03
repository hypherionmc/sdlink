/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging.discord;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.managers.EmbedManager;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.util.DestinationHolder;
import com.hypherionmc.sdlink.util.SDLinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
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
            runAfterSend();
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                BotController.INSTANCE.getLogger().error("Failed to send Discord Message", e);
            }
        }
    }

    /**
     * Send a Non Console relay message to discord
     */
    private void sendNormalMessage() {
        DestinationHolder channel = resolveDestination();

        // Check if a webhook is configured, and use that instead
        if (channel.hasWebhook() && SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.enabled) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();

            if (messageType == MessageType.CHAT) {
                builder.setUsername(SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.webhookNameFormat.replace("%display_name%", this.author.getDisplayName()).replace("%mc_name%", this.author.getUsername()));
            } else {
                builder.setUsername(this.author.getDisplayName());
            }

            if (!this.author.getAvatar().isEmpty()) {
                builder.setAvatarUrl(this.author.getAvatar());
            }

            // Message must be an Embed
            if (channel.useEmbed()) {
                EmbedBuilder eb = buildEmbed(false, channel.embedLayout());
                WebhookEmbed web = WebhookEmbedBuilder.fromJDA(eb.build()).build();
                builder.addEmbeds(web);
            } else {
                builder.setContent(message);
            }

            channel.webhook().send(builder.build()).thenRun(this::runAfterSend);
        } else {
            if (channel.channel() == null) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging)
                    BotController.INSTANCE.getLogger().warn("Expected to get Channel for {}, but got null", messageType.name());
                runAfterSend();
                return;
            }

            // Use the configured channel instead
            if (channel.useEmbed()) {
                EmbedBuilder eb = buildEmbed(true, channel.embedLayout());
                channel.channel().sendMessageEmbeds(eb.build()).queue(success -> runAfterSend());
            } else {
                channel.channel().sendMessage(
                                this.messageType == MessageType.CHAT ?
                                        SDLinkConfig.INSTANCE.messageFormatting.chat.replace("%player%", author.getDisplayName()).replace("%mcname%", author.getProfile() == null ? "Unknown" : author.getProfile().getName()).replace("%message%", message)
                                        : message)
                        .queue(success -> runAfterSend());
            }
        }
    }

    private void runAfterSend() {
        if (afterSend != null)
            afterSend.run();
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
    private DestinationHolder resolveDestination() {
        MessageChannelConfig.DestinationObject destinationObject = CacheManager.messageDestinations.get(messageType);
        if (destinationObject != null) {
            return destinationObject.toHolder(messageType);
        }

        // This code should never be reached, but it's added here as a fail-safe
        return SDLinkConfig.INSTANCE.messageDestinations.chat.toHolder(MessageType.CHAT);
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
