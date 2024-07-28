/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.core.messaging.MessageDestination;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * @author HypherionSA
 * Hook class to handle messages the bot receives
 */
public class DiscordMessageHooks {

    /**
     * Chat messages to be sent back to discord
     */
    public static void discordMessageEvent(MessageReceivedEvent event) {
        try {
            if (!event.getChannel().getId().equalsIgnoreCase(SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID))
                return;

            GuildMessageChannel channel = ChannelManager.getDestinationChannel(MessageDestination.CHAT);

            if (channel == null) {
                BotController.INSTANCE.getLogger().warn("Tried to relay discord message before bot is ready. Aborting");
                return;
            }

            if (event.getChannel().getIdLong() != channel.getIdLong())
                return;

            if (HiddenPlayersManager.INSTANCE.isPlayerHidden(event.getMember().getId()))
                return;

            if (event.getAuthor().isBot() && SDLinkConfig.INSTANCE.chatConfig.ignoreBots)
                return;

            if (SDLinkConfig.INSTANCE.linkedCommands.enabled && !SDLinkConfig.INSTANCE.linkedCommands.permissions.isEmpty() && event.getMessage().getContentRaw().startsWith(SDLinkConfig.INSTANCE.linkedCommands.prefix))
                return;

            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                BotController.INSTANCE.getLogger().info("Sending Message from {}: {}", event.getAuthor().getName(), event.getMessage().getContentStripped());
            }

            String message = event.getMessage().getContentDisplay();
            if (message.isEmpty() && !event.getMessage().getAttachments().isEmpty()) {
                message = (long) event.getMessage().getAttachments().size() + " attachments";
            }

            if (!event.getMessage().getContentDisplay().isEmpty() && !event.getMessage().getAttachments().isEmpty()) {
                message = message + " (+" + (long) event.getMessage().getAttachments().size() + " attachments)";
            }

            if (message.isEmpty())
                return;

            if (event.getMessage().getReferencedMessage() != null && !event.getMessage().isWebhookMessage()) {
                try {
                    message = "Replied to " + event.getMessage().getReferencedMessage().getMember().getEffectiveName() + ": " + message;
                } catch (Exception e) {
                    if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                        e.printStackTrace();
                    }
                }
            }

            SDLinkPlatform.minecraftHelper.discordMessageReceived(event.getMember(), message);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to process discord message", e);
        }
    }

}
