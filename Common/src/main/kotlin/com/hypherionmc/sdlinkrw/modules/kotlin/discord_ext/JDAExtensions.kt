package com.hypherionmc.sdlinkrw.modules.kotlin.discord_ext

import com.hypherionmc.sdlink.core.discord.BotController
import com.hypherionmc.sdlinkrw.modules.cache.discord.DiscordServer
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import java.util.concurrent.TimeUnit

fun Guild.fromCache(): DiscordServer? {
    return SDLCache.getServer(this)
}

fun Guild.memberFromCache(id: String): Member? {
    return SDLCache.getMemberById(id, this)
}

fun Member.getAllRoles() = SDLCache.getAllRolesForUser(this.user)

fun ButtonEmbedPaginator.Builder.defaultBuilder() : ButtonEmbedPaginator.Builder {
    return ButtonEmbedPaginator.Builder()
        .setTimeout(1, TimeUnit.MINUTES)
        .setEventWaiter(BotController.INSTANCE.eventWaiter)
        .waitOnSinglePage(false)
        .setFinalAction { m: Message -> m.editMessageComponents().queue() }
}