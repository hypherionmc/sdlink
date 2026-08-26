package com.hypherionmc.sdlinkrw.modules.translations

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hypherionmc.sdlinkrw.SDLinkConstants
import org.apache.commons.io.IOUtils
import java.io.*
import java.lang.reflect.Type

object TranslationManager {

    private var translations = mutableMapOf<String, String>()
    private val type: Type = object : TypeToken<MutableMap<String, String>>() {}.type
    private val gson = Gson()

    /**
     * Load a translation file
     *
     * @param lang The Language Code to load translations for
     */
    fun loadTranslations(lang: String) {
        translations.clear()
        loadTranslatedFile(lang)
    }

    /**
     * Internal method to load translations from files.
     *
     * @param lang The Language Code to load translations for
     */
    private fun loadTranslatedFile(lang: String) {
        val f = File("config/simple-discord-link/language/$lang.json")
        f.getParentFile().mkdirs()

        if (!f.exists() && lang == "en_us") {
            if (createFileFromResource(f)) {
                loadFromFile(f)
                return
            }
        }

        if (loadFromResource("assets/sdlink/lang/$lang.json")) {
            return
        }

        SDLinkConstants.LOGGER.warn("Failed to load translation for {}. Falling back to en_us.", lang)
        loadFromResource("assets/sdlink/lang/en_us.json")
    }

    /**
     * Load and parse a JSON file into a usable language map
     *
     * @param file The File to load from the disk
     */
    private fun loadFromFile(file: File) {
        try {
            FileReader(file).use { reader -> translations = gson.fromJson<MutableMap<String, String>>(reader, type) }
        } catch (e: IOException) {
            SDLinkConstants.LOGGER.error("Failed to load translation file: {}", file.absolutePath, e)
        }
    }

    /**
     * Load and parse a JSON file from the Jar resources
     *
     * @param resourcePath The path in the jar to load the resource from
     * @return True if loaded, false if the file does not exist or failed to load
     */
    private fun loadFromResource(resourcePath: String): Boolean {
        try {
            javaClass.getClassLoader().getResourceAsStream(resourcePath).use { inputStream ->
                if (inputStream == null) return false
                val reader = InputStreamReader(inputStream)
                translations = gson.fromJson<MutableMap<String, String>>(reader, type)
                return true
            }
        } catch (e: IOException) {
            SDLinkConstants.LOGGER.error("Failed to load resource translation file: {}", resourcePath, e)
        }
        return false
    }

    private fun createFileFromResource(targetFile: File): Boolean {
        try {
            javaClass.getClassLoader().getResourceAsStream("assets/sdlink/lang/en_us.json").use { inputStream ->
                if (inputStream == null) {
                    SDLinkConstants.LOGGER.error("Default translation file {} not found in resources!", "assets/sdlink/lang/en_us.json")
                    return false
                }

                FileOutputStream(targetFile).use { outputStream ->
                    IOUtils.copy(inputStream, outputStream)
                }

                SDLinkConstants.LOGGER.info("Default translation file created at {}", targetFile.getAbsolutePath())
                return true
            }
        } catch (e: IOException) {
            SDLinkConstants.LOGGER.error("Failed to create default translation file {}", targetFile.getAbsolutePath(), e)
            return false
        }
    }

    /**
     * Try to convert a language key into human-readable text
     *
     * @param key The translation key to resolve
     * @return The parsed text, or the translation key if the value does not exist
     */
    fun translate(key: String): String {
        return translations.getOrDefault(key, key)
    }

}