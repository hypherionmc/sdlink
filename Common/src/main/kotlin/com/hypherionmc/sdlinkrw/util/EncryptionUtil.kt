package com.hypherionmc.sdlinkrw.util

import com.hypherionmc.sdlink.core.discord.BotController
import com.hypherionmc.sdlinkrw.SDLinkConstants
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.saltString
import org.apache.commons.io.FileUtils
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import java.io.File
import java.nio.charset.StandardCharsets

class EncryptionUtil(encCode: String) {
    private val prefix = "enc:"
    private val canRun = encCode.isNotEmpty()

    private val encryptor = StandardPBEStringEncryptor().apply {
        if (canRun) {
            setPassword(encCode)
        }
    }

    init {
        if (!canRun) {
            SDLinkConstants.LOGGER.error("Failed to initialize encryption system. Your config values will not be encrypted!")
        }
    }

    /**
     * Encrypt a string if not already encrypted
     */
    fun encrypt(input: String): String {
        if (!canRun) {
            return input
        }

        if (isEncrypted(input)) {
            return input
        }

        return encryptor.encrypt(prefix + input)
    }

    /**
     * Decrypt an encrypted string
     */
    fun decrypt(input: String): String {
        if (!canRun || !isEncrypted(input)) {
            return input
        }

        return internalDecrypt(input)
            .removePrefix(prefix)
    }

    /**
     * Internal decrypt helper
     */
    private fun internalDecrypt(input: String): String {
        if (!canRun) {
            return input
        }

        return encryptor.decrypt(input)
    }

    /**
     * Test if string is encrypted
     */
    private fun isEncrypted(input: String): Boolean {
        return try {
            internalDecrypt(input).startsWith(prefix)
        } catch (_: EncryptionOperationNotPossibleException) {
            false
        }
    }

    companion object {
        @JvmField
        val INSTANCE: EncryptionUtil = createInstance()

        private fun createInstance(): EncryptionUtil {
            var encCode = ""
            val storageDir = File("sdlinkstorage")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            try {
                val encKey = File(storageDir, "sdlink.enc")

                if (!encKey.exists()) {
                    FileUtils.writeStringToFile(encKey, String.saltString(), StandardCharsets.UTF_8)
                }

                encCode = FileUtils.readFileToString(encKey, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                SDLinkConstants.LOGGER.error("Failed to initialize Encryption", e)
            }

            return EncryptionUtil(encCode)
        }
    }
}