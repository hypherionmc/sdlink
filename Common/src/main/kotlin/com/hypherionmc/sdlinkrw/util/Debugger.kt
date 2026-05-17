package com.hypherionmc.sdlinkrw.util

import com.hypherionmc.sdlinkrw.SDLinkConstants
import io.sentry.Sentry
import io.sentry.SentryOptions
import java.io.File

/**
 * @author HypherionSA
 *
 * A debugging logger that will absolutely spam the hell out of the console and logs.
 * Used during development and severe fault finding to get an internal insight into what the code is doing
 */
object Debugger {

    private val stackWalker = StackWalker.getInstance()
    private val logger = SDLinkConstants.LOGGER
    private val marker = File("./sdlinkstorage/IAMDADEV")

    /**
     * Log a message to the console and the log file.
     *
     * @param message The message to log
     */
    fun log(message: String) {
        val frame = stackWalker.walk { it.skip(1).findFirst() }.orElse(null) ?: return
        if (!marker.exists()) return

        logger.warn("[${frame.className}#${frame.methodName}:${frame.lineNumber}] $message")
    }

    /**
     * Log a formatted message to the console and the log file.
     *
     * @param message The message to log
     * @param args The arguments to format the message with
     */
    fun log(message: String, vararg args: Any) {
        val frame = stackWalker.walk { it.skip(1).findFirst() }.orElse(null) ?: return
        if (!marker.exists()) return

        logger.warn("[${frame.className}#${frame.methodName}:${frame.lineNumber}] $message", args)
    }

    /**
     * Temporary. To be removed on full release
     */
    fun setupSentry() {
        Sentry.init { options: SentryOptions ->
            options.dsn = "https://cbd223e80933420d92fc7c82a347de0b@sink.firstdark.dev/4"
            options.environment = "experimental"

            options.tracesSampleRate = 0.0
            options.isAttachStacktrace = true
            options.isSendDefaultPii = false
            options.isAttachServerName = false
        }
    }

}