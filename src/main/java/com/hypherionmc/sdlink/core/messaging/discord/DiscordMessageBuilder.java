/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging.discord;

import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.accounts.DiscordUser;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageIgnoreConfig;
import com.hypherionmc.sdlink.core.messaging.MessageType;

import java.util.regex.Pattern;

/**
 * @author HypherionSA
 * Used to construct a {@link DiscordMessage} to be sent back to discord
 */
public final class DiscordMessageBuilder {

    private final MessageType messageType;
    private DiscordAuthor author;
    private String message;
    private Runnable afterSend;

    /**
     * Construct a discord message
     *
     * @param messageType The type of message being sent
     */
    public DiscordMessageBuilder(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Add an Author to the message
     */
    public DiscordMessageBuilder author(DiscordAuthor author) {
        this.author = author;

        if (author.getUsername().equalsIgnoreCase("server")) {
            this.author = DiscordAuthor.SERVER;
        }

        if (SDLinkConfig.INSTANCE.chatConfig.useLinkedNames && this.author != DiscordAuthor.SERVER) {
            MinecraftAccount account = author.getProfile() != null ? MinecraftAccount.of(author.getProfile()) : MinecraftAccount.of(author.getUsername());
            DiscordUser discordUser = account.getDiscordUser();

            if (account != null && discordUser != null) {
                this.author = DiscordAuthor.of(discordUser.getEffectiveName(), discordUser.getAvatarUrl(), author.getUsername(), false);
            }
        }

        return this;
    }

    /**
     * The Actual message that will be sent
     */
    public DiscordMessageBuilder message(String message) {
        if (this.messageType == MessageType.CHAT) {
            //using regex to replace @here and @everyone mentions
            message = Pattern.compile("@+(here|everyone)").matcher(message).replaceAll("");

            if (!SDLinkConfig.INSTANCE.chatConfig.allowMentionsFromChat) {
                //using regex to replace any mention
                message = Pattern.compile("<[^>]*\\d+>").matcher(message).replaceAll("");
            }
        }

        if (SDLinkConfig.INSTANCE.ignoreConfig.enabled) {
            for (MessageIgnoreConfig.Ignore i : SDLinkConfig.INSTANCE.ignoreConfig.entries) {
                if (i.searchMode == MessageIgnoreConfig.FilterMode.MATCHES && message.equalsIgnoreCase(i.search)) {
                    if (i.action == MessageIgnoreConfig.ActionMode.REPLACE) {
                        message = message.replace(i.search, i.replace);
                    } else {
                        message = "";
                    }
                }

                if (i.searchMode == MessageIgnoreConfig.FilterMode.CONTAINS && message.contains(i.search)) {
                    if (i.action == MessageIgnoreConfig.ActionMode.REPLACE) {
                        message = message.replace(i.search, i.replace);
                    } else {
                        message = "";
                    }
                }

                if (i.searchMode == MessageIgnoreConfig.FilterMode.STARTS_WITH && message.startsWith(i.search)) {
                    if (i.action == MessageIgnoreConfig.ActionMode.REPLACE) {
                        message = message.replace(i.search, i.replace);
                    } else {
                        message = "";
                    }
                }
            }
        }

        this.message = message;
        return this;
    }

    public DiscordMessageBuilder afterSend(Runnable afterSend) {
        this.afterSend = afterSend;
        return this;
    }

    /**
     * Build a Discord Message ready to be sent
     */
    public DiscordMessage build() {
        if (this.author == null) {
            this.author = DiscordAuthor.SERVER;
        }

        if (this.message == null) {
            this.message = "";
        }

        return new DiscordMessage(this);
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public DiscordAuthor getAuthor() {
        return author;
    }

    public Runnable getAfterSend() {
        return afterSend;
    }
}
