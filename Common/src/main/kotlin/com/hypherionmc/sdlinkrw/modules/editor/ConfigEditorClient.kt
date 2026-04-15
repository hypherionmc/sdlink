package com.hypherionmc.sdlinkrw.modules.editor

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hypherionmc.craterlib.api.game.commands.CraterCommandSourceStack
import com.hypherionmc.craterlib.api.game.text.Text
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlink.core.discord.BotController
import com.hypherionmc.sdlink.util.EncryptionUtil
import com.hypherionmc.sdlinkrw.modules.editor.responses.SocketResponse
import com.neovisionaries.ws.client.*
import net.kyori.adventure.text.event.ClickEvent
import org.apache.commons.io.FileUtils
import java.nio.charset.StandardCharsets

class ConfigEditorClient {
    private var webSocket: WebSocket? = null

    fun openConnection(sourceStack: CraterCommandSourceStack) {
        val identifier = EncryptionUtil.getSaltString()

        try {
            closeServer()
            webSocket = WebSocketFactory().createSocket("wss://editor.firstdark.dev/ws/config?identifier=$identifier")
            webSocket!!.pingInterval = 10000
            webSocket!!.addListener(ConfigEditorWSEvents(identifier, sourceStack))
            webSocket!!.connect()
        } catch (e: Exception) {
            BotController.INSTANCE.logger.error("Failed to open connection to Config Editor", e)
        }
    }

    fun closeServer() {
        try {
            if (webSocket != null && webSocket!!.isOpen) webSocket!!.disconnect()
        } catch (_: Exception) { }
    }

    companion object {
        @JvmField
        val INSTANCE: ConfigEditorClient = ConfigEditorClient()
    }

    private class ConfigEditorWSEvents(val identifier: String, val sourceStack: CraterCommandSourceStack) : WebSocketAdapter() {
        private val GSON: Gson = GsonBuilder().serializeNulls().create()

        override fun onConnected(webSocket: WebSocket?, map: MutableMap<String?, MutableList<String?>?>?) {
            BotController.INSTANCE.logger.info("Editor Websocket connected")
        }

        @Throws(java.lang.Exception::class)
        override fun onTextFrame(webSocket: WebSocket, webSocketFrame: WebSocketFrame) {
            val response: SocketResponse = GSON.fromJson<SocketResponse>(webSocketFrame.payloadText, SocketResponse::class.java)

            if (response.socketCode.equals("WS_WAITING", ignoreCase = true)) {
                sourceStack.sendMessage(
                    Text
                        .literal(String.format("Editor Connection Ready. Visit https://editor.firstdark.dev/%s to get started", identifier))
                        .clickEvent(ClickEvent.openUrl(String.format("https://editor.firstdark.dev/%s", identifier)))
                )

                BotController.INSTANCE.logger.info("Editor Connection Ready. Visit https://editor.firstdark.dev/{} to get started", identifier)
            }

            if (response.socketCode.equals("WS_GET_CONFIG", ignoreCase = true)) {
                val ec = EncryptionUtil(identifier)
                val config = FileUtils.readFileToString(SDLinkConfig.INSTANCE.configPath, StandardCharsets.UTF_8)
                webSocket.sendText(GSON.toJson(SocketResponse("WS_SEND_CONFIG", ec.encrypt(config))))
            }

            if (response.socketCode.equals("WS_SAVE_CONFIG", ignoreCase = true)) {
                BotController.INSTANCE.logger.info("Got Config update from editor")

                val ec = EncryptionUtil(identifier)
                val config = ec.decrypt(response.message)
                FileUtils.writeStringToFile(SDLinkConfig.INSTANCE.configPath, config, StandardCharsets.UTF_8)
                SDLinkConfig.INSTANCE.configReloaded()
            }
        }

        override fun onConnectError(webSocket: WebSocket?, e: WebSocketException?) {
            BotController.INSTANCE.logger.error("Failed to connect to editor web socket", e)
        }

        override fun onDisconnected(webSocket: WebSocket, webSocketFrame: WebSocketFrame, webSocketFrame1: WebSocketFrame, b: Boolean) {
            BotController.INSTANCE.logger.warn(
                "Disconnected from Editor Websocket with code {}: {}",
                webSocketFrame.closeCode,
                webSocketFrame.closeReason
            )
        }

        override fun onCloseFrame(webSocket: WebSocket?, webSocketFrame: WebSocketFrame) {
            BotController.INSTANCE.logger.warn(
                "Connection from Editor Terminated with code {}: {}",
                webSocketFrame.closeCode,
                webSocketFrame.closeReason
            )
        }
    }
}