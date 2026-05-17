/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.verification;

import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public final class UnverifyAccountSlashCommand extends SDLinkSlashCommand {

    public UnverifyAccountSlashCommand() {
        super(false);
        this.name = "unverify";
        this.help = SDText.translate("command.unverify.help").toString();
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!SDLinkConfig.INSTANCE.accessControl.allowVerifyInDm && !event.isFromGuild()) {
            return;
        }

        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();

        List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

        if (accounts.isEmpty()) {
            event.getHook().sendMessage(SDText.translate("error.no_db_accounts").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        Guild guild = event.isFromGuild() ? event.getGuild() : (event.getJDA().getGuilds().isEmpty() ? null : event.getJDA().getGuilds().get(0));
        if (guild == null) {
            event.getHook().sendMessage(SDText.translate("error.no_discord_server").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        Member m = event.isFromGuild() ? event.getMember() : guild.getMemberById(event.getUser().getId());
        if (m == null) {
            event.getHook().sendMessage(SDText.translate("error.not_a_member_of", guild.getName()).toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        boolean didUnverify = false;

        for (SDLinkAccount account : accounts) {
            if (account.getDiscordId() != null && account.getDiscordId().equalsIgnoreCase(m.getId())) {
                MinecraftAccount minecraftAccount = MinecraftAccount.of(account);
                Result result = minecraftAccount.unverifyAccount(m, guild);
                event.getHook().sendMessage(result.getMessage()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
                didUnverify = true;
                break;
            }
        }

        if (!didUnverify)
            event.getHook().sendMessage(SDText.translate("command.unverify.failed").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
    }

}