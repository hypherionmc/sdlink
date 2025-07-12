/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.MessageContext;
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
import com.hypherionmc.sdlink.util.PKUtil;
import com.hypherionmc.sdlink.util.translations.Text;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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

            if (event.isWebhookMessage() || event.getAuthor().isBot()) {
                if (!(SDLinkConfig.INSTANCE.chatConfig.pluralKitCompat && PKUtil.isPK(event.getMessageId())) &&
                        SDLinkConfig.INSTANCE.chatConfig.ignoreBots
                ) {
                    return;
                }
            }

            if (!(event.isWebhookMessage() || event.getAuthor().isBot()) && SDLinkConfig.INSTANCE.chatConfig.pluralKitCompat && PKUtil.isPK(event.getMessageId()))
                return;


            if (SDLinkConfig.INSTANCE.linkedCommands.enabled && !SDLinkConfig.INSTANCE.linkedCommands.permissions.isEmpty() && event.getMessage().getContentRaw().startsWith(SDLinkConfig.INSTANCE.linkedCommands.prefix))
                return;


            SDLinkPlatform.minecraftHelper.discordMessageReceived(MessageContext.of(member, event.getMessage()));
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
