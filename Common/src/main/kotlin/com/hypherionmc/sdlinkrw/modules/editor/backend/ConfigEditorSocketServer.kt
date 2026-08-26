package com.hypherionmc.sdlinkrw.modules.editor.backend

import com.google.gson.Gson
import com.hypherionmc.craterlib.api.game.commands.CraterCommandSourceStack
import com.hypherionmc.craterlib.api.game.text.Text
import com.hypherionmc.craterlib.libs.kyori.adventure.text.event.ClickEvent
import com.hypherionmc.sdlinkrw.SDLinkConstants
import com.hypherionmc.sdlinkrw.modules.editor.ConfigEditorServer
import com.hypherionmc.sdlinkrw.modules.editor.models.ConfigSocketCode
import com.hypherionmc.sdlinkrw.modules.editor.models.ConfigSocketMessage
import com.hypherionmc.tinyserver.connection.WebSocketSession
import com.hypherionmc.tinyserver.handler.WebsocketHandler
import com.hypherionmc.tinyserver.routing.TinyPathHandler

class ConfigEditorSocketServer(app: TinyPathHandler, val editor: ConfigEditorServer, val sourceStack: CraterCommandSourceStack?, val token: String): ConfigEditorSocketHandler() {

    private val tokenExpiry = System.currentTimeMillis() + 60_000
    private var sess: Session? = null
    private var lastUsedToken = ""

    init {
        val handler = object : WebsocketHandler() {
            override fun onClose(session: WebSocketSession) {
                sess = null
                editor.closeServer()
            }

            override fun onMessage(session: WebSocketSession, message: String) {
                if (sess == null) return
                handleMessage(sess!!, message)
            }

            override fun onOpen(session: WebSocketSession) {
                if (sess != null) return
                sess = Session(session, gson)
                onConnect()
            }
        }

        app.addPrefixPath("/ws/config", handler)
        socketReady()
    }

    private fun onConnect() {
        val token = sess!!.queryParam("identifier")

        if (token.isNullOrBlank()) {
            sess!!.close(1003, "Invalid Connection Request")
            return
        }

        if (token == lastUsedToken) {
            sess!!.send(ConfigSocketMessage(ConfigSocketCode.INVALID_SESSION, "Token is invalid"))
            return
        }

        if (tokenExpiry < System.currentTimeMillis() || token == lastUsedToken) {
            sess!!.close(1003, "Expired Token")
            return
        }

        lastUsedToken = token
        sess!!.send(ConfigSocketMessage(ConfigSocketCode.CONNECTED, token))
    }

    override fun isSessionValid(id: String): Boolean {
        return id == token
    }

    override fun socketReady() {
        val link = "http://YOURSERVERIP/$token"

        sourceStack?.sendMessage(
            Text.literal("Editor Connection Ready. Visit $link to get started")
                .clickEvent(ClickEvent.openUrl(link))
        )

        SDLinkConstants.LOGGER.info("Editor Connection Ready. Visit $link to get started")
    }

    internal class Session(val channel: WebSocketSession, val gson: Gson): ChannelHandler {
        override fun send(message: ConfigSocketMessage) {
            channel.send(gson.toJson(message))
        }

        override fun queryParam(name: String): String? {
            return channel.queryParam(name)
        }

        override fun close(code: Int, reason: String) {
            channel.close(code, reason)
        }
    }
}