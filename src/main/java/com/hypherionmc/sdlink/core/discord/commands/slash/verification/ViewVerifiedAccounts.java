/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.verification;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.util.MessageUtil;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hypherionmc.sdlink.core.managers.DatabaseManager.sdlinkDatabase;

/**
 * @author HypherionSA
 * Staff Command to view a list of Linked Minecraft and Discord accounts
 */
public class ViewVerifiedAccounts extends SDLinkSlashCommand {

    public ViewVerifiedAccounts() {
        super(true);

        this.name = "verifiedaccounts";
        this.help = "View a list of verified Minecraft Accounts";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        try {
            ButtonEmbedPaginator.Builder paginator = MessageUtil.defaultPaginator();

            sdlinkDatabase.reloadCollection("verifiedaccounts");
            List<SDLinkAccount> accounts = sdlinkDatabase.findAll(SDLinkAccount.class);

            EmbedBuilder builder = new EmbedBuilder();
            ArrayList<MessageEmbed> pages = new ArrayList<>();
            AtomicInteger count = new AtomicInteger();

            if (accounts.isEmpty()) {
                event.getHook().sendMessage("There are no verified accounts for this discord").setEphemeral(true).queue();
                return;
            }

            MessageUtil.listBatches(accounts, 10).forEach(itm -> {
                count.getAndIncrement();
                builder.clear();
                builder.setTitle("Verified Accounts - Page " + count + "/" + (int) Math.ceil(((float) accounts.size() / 10)));
                builder.setColor(Color.GREEN);
                StringBuilder sBuilder = new StringBuilder();

                itm.forEach(v -> {
                    Member member = null;

                    if (v.getDiscordID() != null && !v.getDiscordID().isEmpty()) {
                        member = event.getGuild().getMemberById(v.getDiscordID());
                    }

                    sBuilder.append(v.getUsername()).append(" -> ").append(member == null ? "Unlinked" : member.getAsMention()).append("\r\n");
                });
                builder.setDescription(sBuilder);
                pages.add(builder.build());
            });

            paginator.setItems(pages);
            ButtonEmbedPaginator embedPaginator = paginator.build();

            event.getHook().sendMessageEmbeds(pages.get(0)).setEphemeral(false).queue(success -> embedPaginator.paginate(success, 1));
        } catch (Exception e) {
            event.getHook().sendMessage("Failed to execute command. Please see your server log").setEphemeral(true).queue();
            if (SDLinkConfig.INSTANCE.generalConfig.debugging)
                e.printStackTrace();
        }
    }

}