package com.hypherionmc.sdlinkrw.api.accounts

data class DiscordUser(
    var effectiveName: String,
    var avatarUrl: String,
    var userId: Long,
    var asMention: String,
    var roleColor: Int
) {

    companion object {
        @JvmStatic
        fun of(name: String, avatarUrl: String, userId: Long, asMention: String, roleColor: Int): DiscordUser {
            return DiscordUser(name, avatarUrl, userId, asMention, roleColor)
        }
    }

}
