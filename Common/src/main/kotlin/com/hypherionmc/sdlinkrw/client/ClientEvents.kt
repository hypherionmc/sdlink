package com.hypherionmc.sdlinkrw.client

import com.hypherionmc.sdlink.networking.SDLinkNetworking

object ClientEvents {

    @JvmField
    var mentionsEnabled: Boolean = false

    fun init() {
        SDLinkNetworking.registerPackets()
    }

}