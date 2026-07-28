/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.hypherionmc.sdlink.api.messaging.MessageContext;
import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.SDLWebhookServerMember;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.server.SDLinkMinecraftBridge;
import com.hypherionmc.sdlink.util.PKUtil;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import com.hypherionmc.sdlinkrw.modules.cache.discord.WebhookCluster;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;
import com.hypherionmc.sdlinkrw.util.Debugger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.EnumSet;
import java.util.List;

/**
 * @author HypherionSA
 * Hook class to handle messages the bot receives
 */
public final class DiscordMessageHooks {

    /**
     * Chat messages to be sent back to discord
     */
    public static void discordMessageEvent(MessageReceivedEvent event) {
        try {
            if (!SDLinkConfig.INSTANCE.chatConfig.discordMessages)
                return;

            if (!SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID.contains(event.getChannel().getId())) return;

            if (SDLCache.INSTANCE.getChannelDestinations(MessageDestination.CHAT).isEmpty()) {
                SDLinkConstants.LOGGER.warn("There are no chat channels set up! Cannot relay messages.");
                return;
            }

            Member member = event.isWebhookMessage() ? SDLWebhookServerMember.of(event.getMessage().getAuthor(), event.getGuild(), event.getJDA()) : event.getMember();

            if (!event.isWebhookMessage() && HiddenPlayersManager.INSTANCE.isPlayerHidden(member.getId()))
                return;

            if (WebhookCluster.INSTANCE.isAppWebhook(event.getMessage().getAuthor().getIdLong()))
                return;

            if (event.isWebhookMessage() || event.getAuthor().isBot()) {
                boolean pluralKitHandled = SDLinkConfig.INSTANCE.chatConfig.pluralKitCompat && PKUtil.isPK(event);
                if (!pluralKitHandled && SDLinkConfig.INSTANCE.chatConfig.ignoreBots) {
                    return;
                }
            }

            if (!(event.isWebhookMessage() || event.getAuthor().isBot()) && SDLinkConfig.INSTANCE.chatConfig.pluralKitCompat && PKUtil.isPK(event))
                return;

            if (SDLinkConfig.INSTANCE.linkedCommands.enabled && !SDLinkConfig.INSTANCE.linkedCommands.permissions.isEmpty() && event.getMessage().getContentRaw().startsWith(SDLinkConfig.INSTANCE.linkedCommands.prefix))
                return;

            var cloned = cloneMessage(event.getMessage());

            if (!(cloned.getEmbeds().isEmpty() && cloned.getContent().isBlank())) {
                SDLCache.INSTANCE.getChannelDestinations(MessageDestination.CHAT).stream().filter(chan -> chan.getIdLong() != event.getChannel().getIdLong()).forEach(channel -> {
                    Debugger.INSTANCE.log("Relaying message to " + channel.getName() + " (" + channel.getId() + ")");
                    var client = WebhookCluster.INSTANCE.getClient(MessageDestination.CHAT, channel.getIdLong());

                    if (client != null) {
                        var relayMessage = WebhookMessageBuilder.fromJDA(cloned);
                        relayMessage.setUsername(event.getMember().getEffectiveName());
                        relayMessage.setAvatarUrl(event.getMember().getEffectiveAvatarUrl());
                        client.send(relayMessage.build()).thenRun(() -> {});
                    } else {
                        Debugger.INSTANCE.log("No client found for " + channel.getName() + " (" + channel.getId() + ")");
                    }
                });
            }

            SDLinkMinecraftBridge.INSTANCE.discordMessageReceived(MessageContext.of(member, event.getMessage()));
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to process discord message", e);
        }
    }

    private static MessageCreateData cloneMessage(Message message) {
        MessageCreateBuilder data = MessageCreateBuilder.fromMessage(message)
                .setAllowedMentions(EnumSet.allOf(Message.MentionType.class))
                .setAllowedMentions(EnumSet.of(Message.MentionType.CHANNEL));

        if (message.getReferencedMessage() != null && !message.getReferencedMessage().getContentRaw().isBlank()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription(message.getReferencedMessage().getContentRaw());
            data.setEmbeds(embed.build());
        }

        if (data.getContent().isBlank()) {
            data.setContent("-# *via " + message.getGuild().getName() + "*");
        } else {
            data.setContent(data.getContent() + "\r\n-# *via " + message.getGuild().getName() + "*");
        }

        return data.build();
    }

    public static void checkVerification(MessageReceivedEvent event) {
        if (!SDLinkConfig.INSTANCE.accessControl.allowVerifyInDm)
            return;

        String message = event.getMessage().getContentStripped();

        if (message.length() != 4) {
            event.getMessage().reply(SDText.translate("error.code_length")).queue();
            return;
        }

        Guild guild = event.getJDA().getGuilds().isEmpty() ? null : event.getJDA().getGuilds().get(0);
        if (guild == null) {
            event.getMessage().reply(SDText.translate("error.no_discord_server")).queue();
            return;
        }

        Member m = guild.getMemberById(event.getAuthor().getIdLong());
        if (m == null) {
            event.getMessage().reply(SDText.translate("error.not_a_member_of", event.getGuild().getName())).queue();
            return;
        }

        List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

        if (accounts.isEmpty()) {
            event.getMessage().reply(SDText.translate("error.no_db_accounts")).queue();
            return;
        }

        boolean didVerify = false;

        for (SDLinkAccount account : accounts) {
            if (account.getVerifyCode() == null)
                continue;

            if (accounts.stream().anyMatch(a -> a.getDiscordID() != null && a.getDiscordID().equals(m.getId())) && !SDLinkConfig.INSTANCE.accessControl.allowMultipleAccounts) {
                event.getMessage().reply(SDText.translate("command.verify.already_verified")).queue();
                return;
            }

            if (account.getVerifyCode().equalsIgnoreCase(message)) {
                MinecraftAccount minecraftAccount = MinecraftAccount.of(account);
                Result result = minecraftAccount.verifyAccount(m);
                event.getMessage().reply(result.getMessage()).queue();
                didVerify = true;
                break;
            }
        }

        if (!didVerify)
            event.getMessage().reply(SDText.translate("command.verify.failed")).queue();
    }
}
