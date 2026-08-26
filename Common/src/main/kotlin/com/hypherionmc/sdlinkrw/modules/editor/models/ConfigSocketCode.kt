package com.hypherionmc.sdlinkrw.modules.editor.models

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

enum class ConfigSocketCode(val raw: String) {
    VALIDATE_SESSION("WS_CHECK_SESSION"),
    VALID_SESSION("WS_VALID_SESSION"),
    INVALID_SESSION("WS_INVALID_SESSION"),
    GET_CONFIG("WS_GET_CONFIG"),
    SAVE_CONFIG("WS_SAVE_CONFIG"),
    PING("PING"),
    PONG("PONG"),
    NOTIFY("WS_NOTIFY"),
    CONFIG_ERROR("WS_CONFIG_ERROR"),
    CONNECTED("WS_CONNECTED"),
    ERROR("WS_ERROR"),
    UNKNOWN("UNKNOWN"),
    SEND_CONFIG("WS_SEND_CONFIG"),
    SERVER_READY("WS_SERVER_READY");

    override fun toString(): String {
        return raw
    }

    companion object {
        fun fromMessage(msg: String): ConfigSocketCode {
            return entries.find { it.raw == msg } ?: UNKNOWN
        }

        fun getAdapter(): GsonAdapter {
            return GsonAdapter()
        }
    }

    class GsonAdapter: TypeAdapter<ConfigSocketCode>() {
        override fun write(out: JsonWriter, value: ConfigSocketCode) {
            out.value(value.raw)
        }

        override fun read(input: JsonReader): ConfigSocketCode? {
            val value = input.nextString()
            return ConfigSocketCode.fromMessage(value)
        }
    }
}