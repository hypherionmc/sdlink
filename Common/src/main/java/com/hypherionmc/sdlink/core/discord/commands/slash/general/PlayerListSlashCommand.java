/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.general;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.server.SDLinkMinecraftBridge;
import com.hypherionmc.sdlink.util.MessageUtil;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HypherionSA
 * Command to view a list of online players currently on the server
 */
public final class PlayerListSlashCommand extends SDLinkSlashCommand {

    public PlayerListSlashCommand() {
        super(false);

        this.name = "playerlist";
        this.help = SDText.translate("command.playerlist.help").toString();
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();

        try {
            List<MinecraftAccount> players = SDLinkMinecraftBridge.INSTANCE.getOnlinePlayers();

            EmbedBuilder builder = new EmbedBuilder();
            List<MessageEmbed> pages = new ArrayList<>();
            AtomicInteger count = new AtomicInteger();

            if (players.isEmpty()) {
                builder.setTitle(SDText.translate("command.playerlist.title").toString());
                builder.setColor(Color.RED);
                builder.setDescription(SDText.translate("command.playerlist.no_online"));
                event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(true).queue();
                return;
            }

            ButtonEmbedPaginator.Builder paginator = MessageUtil.defaultPaginator();

            /**
             * Use Pagination to avoid message limits
             */
            MessageUtil.listBatches(players, 10).forEach(p -> {
                StringBuilder sb = new StringBuilder();
                count.getAndIncrement();
                builder.clear();
                builder.setTitle(SDText.translate("command.playerlist.title_page", count.get(),(int) Math.ceil(((float) players.size() / 10))).toString());
                builder.setColor(Color.GREEN);
                builder.setFooter(SDText.translate("command.playerlist.footer", SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getLeft(), SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getRight()).toString());

                p.forEach(account -> {
                    sb.append("`").append(account.getUsername()).append("`");

                    if ((SDLinkConfig.INSTANCE.accessControl.enabled || SDLinkConfig.INSTANCE.accessControl.optionalVerification) && account.getDiscordUser() != null) {
                        sb.append(" - ").append(account.getDiscordUser().getAsMention());
                    }
                    sb.append("\r\n");
                });

                builder.setDescription(sb.toString());
                pages.add(builder.build());
            });

            paginator.setItems(pages);
            ButtonEmbedPaginator embedPaginator = paginator.build();

            event.getHook().sendMessageEmbeds(pages.get(0)).setEphemeral(false).queue(success -> embedPaginator.paginate(success, 1));
        } catch (Exception e) {
            event.getHook().sendMessage(SDText.translate("error.command_failed").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            SDLinkConstants.LOGGER.error("Failed to run player list command", e);
        }
    }
}
