/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.messaging.discord;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.managers.EmbedManager;
import com.hypherionmc.sdlink.core.messaging.embeds.DiscordEmbed;
import com.hypherionmc.sdlink.util.DestinationHolder;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import com.hypherionmc.sdlinkrw.modules.cache.discord.WebhookCluster;
import com.hypherionmc.sdlinkrw.util.Debugger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hypherionmc.sdlink.util.SDLinkUtils.getOrElse;
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

        BotController.INSTANCE.getSpamManager().receiveMessage(String.format("%s:%s", this.author.getUsername(), this.message));

        if (BotController.INSTANCE.getSpamManager().isBlocked(String.format("%s:%s", this.author.getUsername(), this.message))) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging)
                SDLinkConstants.LOGGER.warn("Blocked message {} due to spam", message);

            return;
        }

        try {
            if (messageType == MessageType.CONSOLE) {
                sendConsoleMessage();
            } else {
                sendNormalMessage();
            }
        } catch (Exception e) {
            runAfterSend();
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to send Discord Message", e);
            }
        }
    }

    /**
     * Send a Non Console relay message to discord
     */
    private void sendNormalMessage() {
        Debugger.INSTANCE.log("Sending message of type " + messageType.name());
        DestinationHolder channel = resolveDestination();

        try {
            if (channel.channel() == null) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging)
                    SDLinkConstants.LOGGER.warn("Expected to get Channel for {}, but got null", messageType.name());
                runAfterSend();
                return;
            }
            MessageCreateBuilder builder = new MessageCreateBuilder();

            if (messageType == MessageType.START || messageType == MessageType.STOP) {
                builder.setAllowedMentions(EnumSet.allOf(Message.MentionType.class));
            } else if (messageType == MessageType.CHAT && SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
                builder.setAllowedMentions(EnumSet.of(Message.MentionType.USER));
                builder.mentionRoles(getMentionableRoles(message));
            } else {
                builder.setAllowedMentions(EnumSet.noneOf(Message.MentionType.class));
            }

            // Use the configured channel instead
            if (channel.useEmbed()) {
                EmbedBuilder eb = buildEmbed(true, channel.embedLayout());
                builder.setEmbeds(eb.build());
            } else {
                String content = this.messageType == MessageType.CHAT ?
                        SDLinkConfig.INSTANCE.messageFormatting.chat
                                .replace("%player%", author.getDisplayName())
                                .replace("%mcname%", author.getProfile() == null ? "Unknown" : author.getProfile().getName())
                                .replace("%message%", message)
                        : message.replace("%mcname%", author.getProfile() == null ? "Unknown" : author.getProfile().getName());
                builder.setContent(content);
            }

            Debugger.INSTANCE.log("Before Sending");

            // TODO: This needs to be cleaned up
            if (messageType == MessageType.STOP) {
                for (MessageChannel cc : channel.channel()) {
                    sendMessage(channel.destination(), cc, builder, true, message);
                }

                BotController.INSTANCE.shutdownBot(false);
            } else {
                channel.channel().forEach(cc -> {
                    sendMessage(channel.destination(), cc, builder, false, message);
                });
            }
        } catch (Exception e) {
            runAfterSend();
        }
    }

    private void sendMessage(MessageDestination destination, MessageChannel channel, MessageCreateBuilder data, boolean isStopMessage, String rawMessage) {
        Debugger.INSTANCE.log("Sending message to " + destination + " in " + channel.getIdLong());
        var client = !SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.enabled ? null : WebhookCluster.INSTANCE.getClient(destination, channel.getIdLong());
        Debugger.INSTANCE.log("Client: " + (client == null ? "null" : client.isShutdown()));

        if (client != null && !client.isShutdown()) {
            data.setContent(rawMessage);
            var message = WebhookMessageBuilder.fromJDA(data.build());

            if (messageType == MessageType.CHAT) {
                message.setUsername(SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.webhookNameFormat.replace("%display_name%", this.author.getDisplayName().replace("\\_", "_")).replace("%mc_name%", this.author.getUsername()));
            } else {
                message.setUsername(this.author.getDisplayName());
            }

            if (!this.author.getAvatar().isEmpty()) {
                message.setAvatarUrl(this.author.getAvatar());
            }

            var sender = client.send(message.build());
            if (isStopMessage) {
                sender.complete(null);
            } else {
                sender.thenRun(() -> {});
            }
        } else {
            var sender = channel.sendMessage(data.build());
            if (isStopMessage) {
                sender.complete();
            } else {
                sender.queue();
            }
        }
    }

    /**
     * Gets all the roles in a message that are mentionable
     */
    private Set<String> getMentionableRoles(String message) {
        return Message.MentionType.ROLE.getPattern().matcher(message).results()
                .map(match -> match.group(1))
                .filter(this::isRoleMentionable)
                .distinct()
                .limit(100)
                .collect(Collectors.toSet());
    }

    private boolean isRoleMentionable(String roleId) {
        Role role;
        try {
            role = SDLCache.INSTANCE.retrieveRoleById(roleId);
        } catch (NumberFormatException e) {
            return false;
        }
        if (role == null) {
            return false;
        }
        return role.isMentionable();
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

            List<GuildMessageChannel> channel =SDLCache.INSTANCE.getChannelDestinations(MessageDestination.CONSOLE);
            channel.forEach(cc -> cc.sendMessage(
                    new MessageCreateBuilder()
                            .setAllowedMentions(EnumSet.noneOf(Message.MentionType.class))
                            .setContent(this.message)
                            .build()
            ).queue());
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to send console message", e);
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
    @SuppressWarnings("deprecation")
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
                .replace("%author%", StringEscapeUtils.escapeJson(this.author.getDisplayName().replace("\\_", "_")))
                .replace("%avatar%", this.author.getAvatar())
                .replace("%message_contents%", StringEscapeUtils.escapeJson(this.message))
                .replace("%player_avatar%", this.author.getRealPlayerAvatar())
                .replace("%role_color%", String.valueOf(this.author.getColor()))
                .replace("%player_name%", StringEscapeUtils.escapeJson(this.author.getRealPlayerName().replace("\\_", "_")))
                .replace("%current_time%", String.valueOf(Instant.now().getEpochSecond()))
                .replace("%username%", StringEscapeUtils.escapeJson(this.author.getUsername().replace("\\_", "_")));

        DiscordEmbed embed = EmbedManager.gson.fromJson(embedJson, DiscordEmbed.class);
        return fromData(embed);
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
    private EmbedBuilder fromData(@NotNull DiscordEmbed data) {
        Checks.notNull(data, "embed");
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(getOrElse(data.title, null));
        builder.setUrl(getOrElse(data.url, null));
        builder.setDescription(getOrElse(data.description, null));

        if (data.timestamp == 1) {
            builder.setTimestamp(Instant.now());
        } else if (data.timestamp != 0) {
            builder.setTimestamp(OffsetDateTime.parse(String.valueOf(data.timestamp)));
        }

        if (getOrElse(data.color, "#000000").startsWith("#")) {
            builder.setColor(Color.decode(getOrElse(data.color, "#000000")));
        } else {
            builder.setColor(Integer.parseInt(getOrElse(data.color, "#000000"), 16));
        }

        if (data.thumbnail != null) {
            builder.setThumbnail(getOrElse(data.thumbnail.url, null));
        }

        if (data.author != null) {
            builder.setAuthor(getOrElse(data.author.name, null),
                    getOrElse(data.author.url, null),
                    getOrElse(data.author.icon_url, null));
        }

        if (data.footer != null) {
            builder.setFooter(getOrElse(data.footer.text, ""),
                    getOrElse(data.footer.icon_url, null));
        }

        if (data.image != null) {
            builder.setImage(getOrElse(data.image.url, null));
        }

        if (data.fields != null && !data.fields.isEmpty()) {
            for (DiscordEmbed.Field field : data.fields) {
                builder.addField(
                        getOrElse(field.name, ZERO_WIDTH_SPACE),
                        getOrElse(field.value, ZERO_WIDTH_SPACE),
                        field.inline
                );
            }
        }

        return builder;
    }
}
