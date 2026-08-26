package com.hypherionmc.sdlink.core.discord.commands.slash.setup;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
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
            SDLCache.INSTANCE.loadCache(slashCommandEvent.getJDA());
            slashCommandEvent.reply(SDText.translate("command.reloadcache.reloaded").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to reload cache", e);
            slashCommandEvent.reply(SDText.translate("command.reloadcache.not_reloaded").toString()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
        }
    }

}
