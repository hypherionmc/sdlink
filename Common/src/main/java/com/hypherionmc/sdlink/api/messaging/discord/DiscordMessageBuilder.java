/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.messaging.discord;

import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.hypherionmc.sdlinkrw.api.accounts.DiscordUser;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import lombok.Getter;

import java.util.Objects;

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
            this.author = DiscordAuthor.getServer();
        }

        if (SDLinkConfig.INSTANCE.chatConfig.useLinkedNames && !Objects.equals(this.author, DiscordAuthor.getServer()) && author.getProfile() != null) {
            MinecraftAccount account = MinecraftAccount.of(author.getProfile());
            DiscordUser discordUser = account.getDiscordUser();

            if (account != null && discordUser != null) {
                String name = discordUser.getEffectiveName();
                String avatar = discordUser.getAvatarUrl();

                if (SDLinkConfig.INSTANCE.chatConfig.useLinkedAvatar) {
                    this.author.overrideData(name, avatar);
                } else {
                    this.author.overrideData(name);
                }

                this.author.setColor(discordUser.getRoleColor());
            }
        }

        if (this.messageType == MessageType.CHAT
                && SDLinkConfig.INSTANCE.channels.chatMessages.useServerForChat
                && SDLinkConfig.INSTANCE.channels.chatMessages.useFancy
        ) {
            this.author = DiscordAuthor.getServer();
        }

        return this;
    }

    /**
     * The Actual message that will be sent
     */
    public DiscordMessageBuilder message(String message) {
        this.message = SDLinkChatUtils.applyFiltering(message, (i) -> this.messageType == MessageType.CONSOLE ? i.appliesTo.appliesToConsole(i) : i.appliesTo.appliesToChat(i));

        // Strip out Obfuscation formatting symbol to prevent abuse
        this.message = this.message.replace("§k", "#k");

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
            this.author = DiscordAuthor.getServer();
        }

        if (this.message == null) {
            this.message = "";
        }

        return new DiscordMessage(this);
    }
}
