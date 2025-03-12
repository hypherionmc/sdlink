package com.hypherionmc.sdlink.util.translations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author HypherionSA
 * Extended Helper Class to help resolve translation keys into Strings
 */
public final class Text implements CharSequence {

    private final String translatedText;

    private Text(String key, @Nullable Object... args) {
        this.translatedText = (args == null || args.length == 0) ?
                TranslationManager.INSTANCE.translate(key)
                : String.format(TranslationManager.INSTANCE.translate(key), args);
    }

    /**
     * Load a translation key into a string
     *
     * @param key The Translation key to load
     * @return The final text, or the translation key if the text does not exist
     */
    public static Text translate(@NotNull String key) {
        return new Text(key);
    }

    /**
     * Load a translation key into a string
     *
     * @param key The Translation key to load
     * @param args List of objects that will replace placeholders in translated text
     * @return The final text, or the translation key if the text does not exist
     */
    public static Text translate(@NotNull String key, @NotNull Object... args) {
        return new Text(key, args);
    }

    @Override
    public int length() {
        return translatedText.length();
    }

    @Override
    public char charAt(int index) {
        return translatedText.charAt(index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return translatedText.subSequence(start, end);
    }

    @NotNull
    @Override
    public String toString() {
        return translatedText;
    }
}
