package com.hypherionmc.sdlinkrw.modules.translations


/**
 * @author HypherionSA
 *
 * Extended Helper Class to help resolve translation keys into Strings
 */
class SDText private constructor(key: String, vararg args: Any?) : CharSequence {

    private val translatedText: String = if (args == null || args.isEmpty())
        TranslationManager.translate(key) else String.format(TranslationManager.translate(key), *args)

    override fun get(index: Int): Char {
        return translatedText[index]
    }

    override val length: Int get() = translatedText.length

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return translatedText.subSequence(startIndex, endIndex)
    }

    override fun toString(): String {
        return translatedText
    }

    companion object {
        /**
         * Load a translation key into a string
         *
         * @param key The Translation key to load
         * @return The final text, or the translation key if the text does not exist
         */
        @JvmStatic
        fun translate(key: String): SDText {
            return SDText(key)
        }

        /**
         * Load a translation key into a string
         *
         * @param key The Translation key to load
         * @param args List of objects that will replace placeholders in translated text
         * @return The final text, or the translation key if the text does not exist
         */
        @JvmStatic
        fun translate(key: String, vararg args: Any): SDText {
            return SDText(key, *args)
        }
    }
}