package com.hypherionmc.sdlinkrw.modules.editor

import com.hypherionmc.craterlib.api.game.commands.CraterCommandSourceStack
import com.hypherionmc.craterlib.api.util.CraterServiceLoader
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlinkrw.SDLinkConstants
import com.hypherionmc.sdlinkrw.modules.editor.backend.ConfigEditorClient
import com.hypherionmc.sdlinkrw.modules.editor.backend.ConfigEditorSocketServer
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.saltString
import com.hypherionmc.tinyserver.TinyServer
import com.hypherionmc.tinyserver.routing.StaticResourceHandler
import com.hypherionmc.tinyserver.routing.TinyPathHandler
import java.io.IOException

object ConfigEditorServer {

    private var server: TinyServer? = null

    fun createServer(sourceStack: CraterCommandSourceStack?) {
        try {
            closeServer()
            if (SDLinkConfig.INSTANCE.generalConfig.useRemoteEditor) {
                ConfigEditorClient.openConnection(sourceStack)
                return
            }

            val router = TinyPathHandler()
            val resourceHandler = StaticResourceHandler(CraterServiceLoader.loader, "assets/sdlink/frontend")
                .setWelcomeFiles("index.html")
                .setSpaFallback(true)

            router.addPrefixPath("/", resourceHandler)

            server = TinyServer("0.0.0.0", SDLinkConfig.INSTANCE.generalConfig.configEditorPort, router)
            ConfigEditorSocketServer(router, this, sourceStack, String.saltString())
            server!!.start()
        } catch (e: IOException) {
            SDLinkConstants.LOGGER.error("Failed to start editor server", e)
        }
    }

    fun closeServer() {
        server?.stop()
    }
}