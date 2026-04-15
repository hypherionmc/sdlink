package com.hypherionmc.sdlinkrw.modules.kotlin.discord_ext

import com.hypherionmc.sdlinkrw.modules.cache.discord.DiscordServer
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member

fun Guild.fromCache(): DiscordServer? {
    return SDLCache.getServer(this)
}

fun Guild.memberFromCache(id: String): Member? {
    return SDLCache.getMemberById(id, this)
}

fun Member.getAllRoles() = SDLCache.getAllRolesForUser(this.user)