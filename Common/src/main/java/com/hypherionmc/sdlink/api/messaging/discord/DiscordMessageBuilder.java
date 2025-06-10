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
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import lombok.Getter;

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
                this.author.setColor(discordUser.getRoleColor());
            }
        }

        if (this.messageType == MessageType.CHAT
                && SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.useServerForChat
                && SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.enabled
                && !SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.chatWebhook.trim().isEmpty()
        ) {
            this.author = DiscordAuthor.SERVER;
        }

        return this;
    }

    /**
     * The Actual message that will be sent
     */
    public DiscordMessageBuilder message(String message) {
        this.message = SDLinkChatUtils.applyFiltering(
                message,
                (i) -> (i.appliesTo == MessageIgnoreConfig.AppliesTo.DISCORD && (i.target == MessageIgnoreConfig.FilterTarget.CHAT || i.target == MessageIgnoreConfig.FilterTarget.BOTH) || (messageType == MessageType.CONSOLE && i.target == MessageIgnoreConfig.FilterTarget.CONSOLE)),
                (i) -> (i.target != MessageIgnoreConfig.FilterTarget.CONSOLE) && (messageType == MessageType.CONSOLE && i.ignoreConsole));
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
