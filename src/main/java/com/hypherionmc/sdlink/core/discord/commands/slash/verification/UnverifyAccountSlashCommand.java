/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.verification;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.util.List;

import static com.hypherionmc.sdlink.core.managers.DatabaseManager.sdlinkDatabase;

public class UnverifyAccountSlashCommand extends SDLinkSlashCommand {

    public UnverifyAccountSlashCommand() {
        super(false);
        this.name = "unverify";
        this.help = "Unverify your previously verified Minecraft account";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        sdlinkDatabase.reloadCollection("verifiedaccounts");
        List<SDLinkAccount> accounts = sdlinkDatabase.findAll(SDLinkAccount.class);

        if (accounts.isEmpty()) {
            event.getHook().sendMessage("Sorry, but this server does not contain any stored players in its database").setEphemeral(true).queue();
            return;
        }

        boolean didUnverify = false;

        for (SDLinkAccount account : accounts) {
            if (account.getDiscordID() != null && account.getDiscordID().equalsIgnoreCase(event.getMember().getId())) {
                MinecraftAccount minecraftAccount = MinecraftAccount.of(account);
                Result result = minecraftAccount.unverifyAccount(event.getMember(), event.getGuild());
                event.getHook().sendMessage(result.getMessage()).setEphemeral(true).queue();
                didUnverify = true;
                break;
            }
        }

        if (!didUnverify)
            event.getHook().sendMessage("Sorry, we could not un-verify your Minecraft account. Please try again").setEphemeral(true).queue();
    }

}