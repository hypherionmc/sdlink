package com.hypherionmc.sdlink.core.config.impl.channels

import com.hypherionmc.sdlink.api.messaging.MessageType
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

interface IChannelContainer {

    fun channels(): List<GuildMessageChannel>
    fun useEmbed(): Boolean
    fun embedLayout(): String
    fun type(): MessageType
    fun useWebhook(): Boolean
    fun channelsRaw(): MutableList<String>
}