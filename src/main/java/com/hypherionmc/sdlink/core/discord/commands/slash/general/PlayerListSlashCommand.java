/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.general;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import com.hypherionmc.sdlink.core.util.MessageUtil;
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
public class PlayerListSlashCommand extends SDLinkSlashCommand {

    public PlayerListSlashCommand() {
        super(false);

        this.name = "playerlist";
        this.help = "List currently online players on the server";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        try {
            List<MinecraftAccount> players = SDLinkPlatform.minecraftHelper.getOnlinePlayers();

            EmbedBuilder builder = new EmbedBuilder();
            List<MessageEmbed> pages = new ArrayList<>();
            AtomicInteger count = new AtomicInteger();

            if (players.isEmpty()) {
                builder.setTitle("Online Players");
                builder.setColor(Color.RED);
                builder.setDescription("There are currently no players online");
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
                builder.setTitle("Online Players - Page " + count.get() + "/" + (int) Math.ceil(((float) players.size() / 10)));
                builder.setColor(Color.GREEN);

                p.forEach(account -> {
                    sb.append("`").append(account.getUsername()).append("`");

                    if (SDLinkConfig.INSTANCE.accessControl.enabled && account.getDiscordUser() != null) {
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
            event.getHook().sendMessage("Failed to execute command. Please see your server log").setEphemeral(true).queue();
            BotController.INSTANCE.getLogger().error("Failed to run player list command", e);
        }
    }
}
