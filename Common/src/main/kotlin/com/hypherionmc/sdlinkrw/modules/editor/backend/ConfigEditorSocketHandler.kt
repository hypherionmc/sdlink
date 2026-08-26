package com.hypherionmc.sdlinkrw.modules.editor.backend

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlinkrw.modules.editor.ConfigConverter
import com.hypherionmc.sdlinkrw.modules.editor.ConfigConverter.readToJson
import com.hypherionmc.sdlinkrw.modules.editor.ConfigConverter.tomlParser
import com.hypherionmc.sdlinkrw.modules.editor.models.ConfigSocketCode
import com.hypherionmc.sdlinkrw.modules.editor.models.ConfigSocketMessage
import com.hypherionmc.sdlinkrw.modules.editor.models.StandardResponse
import com.neovisionaries.ws.client.WebSocketAdapter
import org.apache.commons.io.FileUtils
import java.nio.charset.StandardCharsets

abstract class ConfigEditorSocketHandler : WebSocketAdapter() {

    protected val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(ConfigSocketCode::class.java, ConfigSocketCode.getAdapter())
        .serializeNulls()
        .create()

    protected fun handleMessage(channel: ChannelHandler, message: String) {
        val id = channel.queryParam("identifier") ?: return
        if (id.isEmpty())
            return

        val msg = try {
            gson.fromJson(message, ConfigSocketMessage::class.java)
        } catch (_: Exception) {
            channel.send(ConfigSocketMessage(ConfigSocketCode.ERROR, "invalid message format"))
            return
        }

        when (msg.socketCode) {
            ConfigSocketCode.VALIDATE_SESSION -> {
                if (!isSessionValid(id)) {
                    channel.send(ConfigSocketMessage(ConfigSocketCode.INVALID_SESSION, ""))
                    return
                }

                channel.send(ConfigSocketMessage(ConfigSocketCode.VALID_SESSION, ""))
                return
            }

            ConfigSocketCode.GET_CONFIG -> {
                val rr = handleUpload(FileUtils.readFileToString(SDLinkConfig.INSTANCE.configPath, StandardCharsets.UTF_8), "simple-discord-link.toml")

                val response: ConfigSocketMessage = if (rr.error) {
                    ConfigSocketMessage(ConfigSocketCode.CONFIG_ERROR, rr.message)
                } else {
                    ConfigSocketMessage(ConfigSocketCode.SEND_CONFIG, gson.toJson(rr.data))
                }

                channel.send(response)
            }

            ConfigSocketCode.SERVER_READY -> {
                socketReady()
            }

            ConfigSocketCode.SAVE_CONFIG -> {
                val configData = gson.fromJson(msg.message, JsonObject::class.java)
                FileUtils.writeStringToFile(SDLinkConfig.INSTANCE.configPath, ConfigConverter.writeToToml(configData), StandardCharsets.UTF_8)
                SDLinkConfig.INSTANCE.configReloaded()
                channel.send(ConfigSocketMessage(ConfigSocketCode.NOTIFY, "Config Saved"))
            }

            ConfigSocketCode.PING -> {
                channel.send(ConfigSocketMessage(ConfigSocketCode.PONG, ""))
            }

            else -> {
                println(msg.socketCode.name)
                channel.send(ConfigSocketMessage(ConfigSocketCode.ERROR, "Unknown command"))
            }
        }
    }

    private fun handleUpload(content: String, fileName: String): StandardResponse {
        try {
            tomlParser.parse(content)
        } catch (_: Exception) {
            return StandardResponse.error("Unsupported or corrupt file")
        }

        try {
            val o: JsonObject = readToJson(content)
            o.addProperty("filename", fileName)
            return StandardResponse.success("Success", o)
        } catch (e: Exception) {
            e.printStackTrace()
            return StandardResponse.error("Failed to process file: " + e.message)
        }
    }

    abstract fun isSessionValid(id: String): Boolean

    abstract fun socketReady()

    interface ChannelHandler {
        fun send(message: ConfigSocketMessage)

        fun close(code: Int, reason: String)

        fun queryParam(name: String): String?
    }
}