package com.hypherionmc.sdlinkrw.modules.editor.models

class StandardResponse internal constructor(val error: Boolean, val message: String, val data: Any?) {

    companion object {
        fun error(message: String): StandardResponse {
            return StandardResponse(true, message, null)
        }

        fun success(message: String): StandardResponse {
            return StandardResponse(false, message, null)
        }

        fun success(message: String, data: Any): StandardResponse {
            return StandardResponse(false, message, data)
        }
    }
}
