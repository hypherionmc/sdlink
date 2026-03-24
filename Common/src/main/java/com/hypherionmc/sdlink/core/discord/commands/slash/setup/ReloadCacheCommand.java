package com.hypherionmc.sdlink.core.discord.commands.slash.setup;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.util.translations.SDText;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public final class ReloadCacheCommand extends SDLinkSlashCommand {

    public ReloadCacheCommand() {
        super(true);
        this.name = "reloadcache";
        this.help = SDText.translate("command.reloadcache.help").toString();
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
        try {
            CacheManager.loadCache();
            ChannelManager.loadChannels();
            slashCommandEvent.reply(SDText.translate("command.reloadcache.reloaded").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to reload cache", e);
            slashCommandEvent.reply(SDText.translate("command.reloadcache.not_reloaded").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
        }
    }

}
