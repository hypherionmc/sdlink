package com.hypherionmc.sdlinkrw.modules.editor.backend

import com.google.gson.Gson
import com.hypherionmc.craterlib.api.game.commands.CraterCommandSourceStack
import com.hypherionmc.craterlib.api.game.text.Text
import com.hypherionmc.craterlib.libs.kyori.adventure.text.event.ClickEvent
import com.hypherionmc.sdlinkrw.SDLinkConstants
import com.hypherionmc.sdlinkrw.modules.editor.models.ConfigSocketMessage
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.saltString
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// TODO: Javadoc when final editor is in
object ConfigEditorClient {
    private var webSocket: WebSocket? = null
    private const val socketUrl = "sdlink.firstdark.dev"

    fun openConnection(sourceStack: CraterCommandSourceStack?) {
        val identifier = String.saltString()

        try {
            closeServer()
            webSocket = WebSocketFactory().createSocket("wss://$socketUrl/ws/remote?identifier=$identifier&version=${URLEncoder.encode(SDLinkConstants.MOD_VERSION, StandardCharsets.UTF_8)}")
            webSocket!!.pingInterval = 10000
            webSocket!!.addListener(ConfigEditorWSEvents(identifier, sourceStack))
            webSocket!!.connect()
        } catch (e: Exception) {
            SDLinkConstants.LOGGER.error("Failed to open connection to Config Editor", e)
        }
    }

    fun closeServer() {
        try {
            if (webSocket != null && webSocket!!.isOpen) webSocket!!.disconnect()
        } catch (_: Exception) { }
    }

    private class ConfigEditorWSEvents(val identifier: String, val sourceStack: CraterCommandSourceStack?) : ConfigEditorSocketHandler() {

        override fun onConnected(webSocket: WebSocket, map: MutableMap<String, MutableList<String>>) {
            SDLinkConstants.LOGGER.info("Editor Websocket connected")
        }

        @Throws(java.lang.Exception::class)
        override fun onTextFrame(webSocket: WebSocket, webSocketFrame: WebSocketFrame) {
            handleMessage(Session(identifier, webSocket, gson), webSocketFrame.payloadText)
        }

        override fun onConnectError(webSocket: WebSocket, e: WebSocketException) {
            SDLinkConstants.LOGGER.error("Failed to connect to editor web socket", e)
        }

        override fun onDisconnected(webSocket: WebSocket, webSocketFrame: WebSocketFrame, webSocketFrame1: WebSocketFrame, b: Boolean) {
            SDLinkConstants.LOGGER.warn(
                "Disconnected from Editor Websocket with code {}: {}",
                webSocketFrame.closeCode,
                webSocketFrame.closeReason
            )
        }

        override fun onCloseFrame(webSocket: WebSocket, webSocketFrame: WebSocketFrame) {
            SDLinkConstants.LOGGER.warn(
                "Connection from Editor Terminated with code {}: {}",
                webSocketFrame.closeCode,
                webSocketFrame.closeReason
            )
        }

        override fun isSessionValid(id: String): Boolean {
            return id == identifier
        }

        override fun socketReady() {
            val link = "https://${socketUrl}/$identifier"

            sourceStack?.sendMessage(
                Text.literal("Editor Connection Ready. Visit $link to get started")
                    .clickEvent(ClickEvent.openUrl(link))
            )

            SDLinkConstants.LOGGER.info("Editor Connection Ready. Visit $link to get started")
        }

        class Session(val id: String, val socket: WebSocket, val gson: Gson) : ChannelHandler {

            override fun send(message: ConfigSocketMessage) {
                socket.sendText(gson.toJson(message))
            }

            override fun close(code: Int, reason: String) {
                socket.sendClose(code, reason)
            }

            override fun queryParam(name: String): String? {
                if (name == "identifier")
                    return id

                return null
            }
        }
    }
}