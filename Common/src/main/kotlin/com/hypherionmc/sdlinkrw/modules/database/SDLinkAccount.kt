package com.hypherionmc.sdlinkrw.modules.database

import com.hypherionmc.sdlink.core.jsondb.annotations.Document
import com.hypherionmc.sdlink.core.jsondb.annotations.Id

@Document(collection = "verifiedaccounts")
data class SDLinkAccount(
    @Id var uuid: String,
    var username: String,
    var inGameName: String,
    var discordID: String?,
    var verifyCode: String?,
    var isOffline: Boolean
) {
    val effectiveInGameName: String = inGameName.ifBlank { username }
}
