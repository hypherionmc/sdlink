/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.messaging.discord;

import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.accounts.DiscordUser;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageIgnoreConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HypherionSA
 * Used to construct a {@link DiscordMessage} to be sent back to discord
 */
@Getter
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

        if (SDLinkConfig.INSTANCE.chatConfig.useLinkedNames && this.author != DiscordAuthor.SERVER && author.getProfile() != null) {
            MinecraftAccount account = MinecraftAccount.of(author.getProfile());
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
        if (SDLinkConfig.INSTANCE.ignoreConfig.enabled) {
            for (MessageIgnoreConfig.Ignore i : SDLinkConfig.INSTANCE.ignoreConfig.entries) {
                if (i.target == MessageIgnoreConfig.FilterTarget.USERNAME)
                    continue;

                boolean isMatch = false;

                switch (i.searchMode) {
                    case MATCHES:
                        isMatch = message.equalsIgnoreCase(i.search);
                        break;

                    case CONTAINS:
                        isMatch = message.contains(i.search);
                        break;

                    case STARTS_WITH:
                        isMatch = message.startsWith(i.search);
                        break;

                    case REGEX:
                        try {
                            Pattern pattern = Pattern.compile(i.search);
                            Matcher matcher = pattern.matcher(message);
                            isMatch = matcher.find();
                        } catch (Exception e) {
                            BotController.INSTANCE.getLogger().error("Invalid regex pattern: {}", i.search);
                        }
                        break;
                }

                if (isMatch) {
                    if (messageType == MessageType.CONSOLE && i.ignoreConsole) {
                        this.message = message;
                        return this;
                    }

                    if (i.action == MessageIgnoreConfig.ActionMode.REPLACE) {
                        message = (i.searchMode == MessageIgnoreConfig.FilterMode.REGEX)
                                ? message.replaceAll(i.search, i.replace)
                                : message.replace(i.search, i.replace);
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
}
