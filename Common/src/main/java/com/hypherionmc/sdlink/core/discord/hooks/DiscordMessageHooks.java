/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.SDLWebhookServerMember;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.core.managers.WebhookManager;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import com.hypherionmc.sdlink.util.translations.Text;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.messages.MessageSnapshot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;

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

            if (!event.getChannel().getId().equalsIgnoreCase(SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID))
                return;

            GuildMessageChannel channel = ChannelManager.getDestinationChannel(MessageDestination.CHAT);

            if (channel == null) {
                BotController.INSTANCE.getLogger().warn("Tried to relay discord message before bot is ready. Aborting");
                return;
            }

            if (event.getChannel().getIdLong() != channel.getIdLong())
                return;

            Member member = event.isWebhookMessage() ? SDLWebhookServerMember.of(event.getMessage().getAuthor(), event.getGuild(), event.getJDA()) : event.getMember();

            if (!event.isWebhookMessage() && HiddenPlayersManager.INSTANCE.isPlayerHidden(member.getId()))
                return;

            if (WebhookManager.isAppWebhook(event.getMessage().getAuthor().getIdLong()))
                return;

            if ((event.isWebhookMessage() || event.getAuthor().isBot()) && SDLinkConfig.INSTANCE.chatConfig.ignoreBots)
                return;

            if (SDLinkConfig.INSTANCE.linkedCommands.enabled && !SDLinkConfig.INSTANCE.linkedCommands.permissions.isEmpty() && event.getMessage().getContentRaw().startsWith(SDLinkConfig.INSTANCE.linkedCommands.prefix))
                return;

            String message = event.getMessage().getContentDisplay();

            MessageReference messageReference = event.getMessage().getMessageReference();
            if (messageReference != null && messageReference.getType() == MessageReference.MessageReferenceType.FORWARD) {
                MessageSnapshot snapshot = event.getMessage().getMessageSnapshots().get(0);
                message = snapshot.getContentRaw();
            }

            String reply = null;
            if (message.isEmpty() && !event.getMessage().getAttachments().isEmpty()) {
                message = (long) event.getMessage().getAttachments().size() + " attachments";
            }

            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                BotController.INSTANCE.getLogger().info("Sending Message from {}: {}", event.getAuthor().getName(), message);
            }

            if (!message.isEmpty() && !event.getMessage().getAttachments().isEmpty()) {
                message = message + " " + Text.translate("message.attachments", (long) event.getMessage().getAttachments().size());
            }

            if (message.isEmpty())
                return;

            if (event.getMessage().getReferencedMessage() != null) {
                try {
                    Member replyMember = event.getMessage().getReferencedMessage().isWebhookMessage() ? SDLWebhookServerMember.of(event.getMessage().getReferencedMessage().getAuthor(), event.getGuild(), event.getJDA()) : event.getMessage().getReferencedMessage().getMember();
                    message = Text.translate("message.replied_to", replyMember.getEffectiveName(), message).toString();
                    reply = event.getMessage().getReferencedMessage().getContentDisplay();
                    reply = EmojiManager.replaceAllEmojis(reply, emoji -> !emoji.getDiscordAliases().isEmpty() ? emoji.getDiscordAliases().get(0) : emoji.getEmoji());
                } catch (Exception e) {
                    if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                        e.printStackTrace();
                    }
                }
            }

            message = EmojiManager.replaceAllEmojis(message, emoji -> !emoji.getDiscordAliases().isEmpty() ? emoji.getDiscordAliases().get(0) : emoji.getEmoji());

            SDLinkPlatform.minecraftHelper.discordMessageReceived(member, message, reply);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to process discord message", e);
        }
    }

    public static void checkVerification(MessageReceivedEvent event) {
        String message = event.getMessage().getContentStripped();

        if (message.length() != 4) {
            event.getMessage().reply(Text.translate("error.code_length")).queue();
            return;
        }

        Guild guild = event.getJDA().getGuilds().isEmpty() ? null : event.getJDA().getGuilds().get(0);
        if (guild == null) {
            event.getMessage().reply(Text.translate("error.no_discord_server")).queue();
            return;
        }

        Member m = guild.getMemberById(event.getAuthor().getIdLong());
        if (m == null) {
            event.getMessage().reply(Text.translate("error.not_a_member_of", event.getGuild().getName())).queue();
            return;
        }

        List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

        if (accounts.isEmpty()) {
            event.getMessage().reply(Text.translate("error.no_db_accounts")).queue();
            return;
        }

        boolean didVerify = false;

        for (SDLinkAccount account : accounts) {
            if (account.getVerifyCode() == null)
                continue;

            if (accounts.stream().anyMatch(a -> a.getDiscordID() != null && a.getDiscordID().equals(m.getId())) && !SDLinkConfig.INSTANCE.accessControl.allowMultipleAccounts) {
                event.getMessage().reply(Text.translate("command.verify.already_verified")).queue();
                return;
            }

            if (account.getVerifyCode().equalsIgnoreCase(message)) {
                MinecraftAccount minecraftAccount = MinecraftAccount.of(account);
                Result result = minecraftAccount.verifyAccount(m, guild);
                event.getMessage().reply(result.getMessage()).queue();
                didVerify = true;
                break;
            }
        }

        if (!didVerify)
            event.getMessage().reply(Text.translate("command.verify.failed")).queue();
    }
}
