package com.hypherionmc.sdlinkrw.modules.database

import com.hypherionmc.sdlink.core.jsondb.annotations.Document
import com.hypherionmc.sdlink.core.jsondb.annotations.Id

@Document(collection = "hiddenplayers")
data class HiddenPlayers(
    @Id var identifier: String,
    var displayName: String,
    var type: String,
) {
    companion object {
        @JvmStatic
        fun of(identifier: String, displayName: String, type: String): HiddenPlayers {
            return HiddenPlayers(identifier, displayName, type)
        }
    }
}
