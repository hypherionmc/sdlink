package com.hypherionmc.sdlink.core.discord.commands.slash.hide;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.HiddenPlayers;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.util.MessageUtil;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HiddenPlayersCommand extends SDLinkSlashCommand {

    public HiddenPlayersCommand() {
        super(true);

        this.name = "hiddenplayers";
        this.help = "List all hidden players on your server";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();

        try {
            HashMap<String, HiddenPlayers> hiddenPlayers = HiddenPlayersManager.INSTANCE.getHiddenPlayers();

            EmbedBuilder builder = new EmbedBuilder();
            List<MessageEmbed> pages = new ArrayList<>();
            AtomicInteger count = new AtomicInteger();

            if (hiddenPlayers.isEmpty()) {
                builder.setTitle("Hidden Players");
                builder.setColor(Color.RED);
                builder.setDescription("There are currently no hidden players");
                event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(true).queue();
                return;
            }

            ButtonEmbedPaginator.Builder paginator = MessageUtil.defaultPaginator();

            /**
             * Use Pagination to avoid message limits
             */
            MessageUtil.listBatches(hiddenPlayers.values().stream().toList(), 10).forEach(p -> {
                StringBuilder sb = new StringBuilder();
                count.getAndIncrement();
                builder.clear();
                builder.setTitle("Hidden Players - Page " + count.get() + "/" + (int) Math.ceil(((float) hiddenPlayers.size() / 10)));
                builder.setColor(Color.GREEN);

                p.forEach(account -> {
                    sb.append("`").append(account.getDisplayName()).append("`");
                    sb.append(" - ").append(account.getType());
                    sb.append("\r\n");
                });

                builder.setDescription(sb.toString());
                pages.add(builder.build());
            });

            paginator.setItems(pages);
            ButtonEmbedPaginator embedPaginator = paginator.build();

            event.getHook().sendMessageEmbeds(pages.get(0)).setEphemeral(false).queue(success -> embedPaginator.paginate(success, 1));
        } catch (Exception e) {
            event.getHook().sendMessage("Failed to execute command. Please see your server log").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            BotController.INSTANCE.getLogger().error("Failed to run hidden player list command", e);
        }
    }
}