package com.hypherionmc.sdlinkrw

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SDLinkConstants {
    const val MOD_ID: String = "sdlink"
    const val MOD_NAME: String = "Simple Discord Link"
    @JvmField val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)
    @JvmField val MOD_VERSION: String = getVersion()

    private fun getVersion(): String {
        return SDLinkConstants::class.java
            .getResourceAsStream("/assets/sdlink/dash_version.txt")
            ?.bufferedReader()
            ?.use { it.readText().trim() }
            ?: "Unknown"
    }
}