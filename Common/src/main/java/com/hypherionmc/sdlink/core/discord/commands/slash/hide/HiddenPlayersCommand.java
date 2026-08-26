package com.hypherionmc.sdlink.core.discord.commands.slash.hide;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.util.MessageUtil;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.modules.database.HiddenPlayers;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class HiddenPlayersCommand extends SDLinkSlashCommand {

    public HiddenPlayersCommand() {
        super(true);

        this.name = "hiddenplayers";
        this.help = SDText.translate("command.hiddenplayers.help").toString();
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
                builder.setTitle(SDText.translate("command.hiddenplayers.title").toString());
                builder.setColor(Color.RED);
                builder.setDescription(SDText.translate("command.hiddenplayers.no_players"));
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
                builder.setTitle(SDText.translate("command.hiddenplayers.title_page", count.get(),(int) Math.ceil(((float) hiddenPlayers.size() / 10))).toString());
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
            event.getHook().sendMessage(SDText.translate("error.command_failed").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            SDLinkConstants.LOGGER.error("Failed to run hidden player list command", e);
        }
    }
}