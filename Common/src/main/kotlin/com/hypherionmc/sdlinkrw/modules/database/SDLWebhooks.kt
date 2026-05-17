package com.hypherionmc.sdlinkrw.modules.database

import com.hypherionmc.sdlink.core.jsondb.annotations.Document
import com.hypherionmc.sdlink.core.jsondb.annotations.Id
import java.util.*

@Document(collection = "webhook_storage")
data class SDLWebhooks(
    @Id var id: UUID,
    var webhookId: String,
    var webhookToken: String,
    var channelId: String,
    var guildId: String,
)
