package com.hypherionmc.sdlink.util.translations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypherionmc.sdlink.core.discord.BotController;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * @author HypherionSA
 * Custom Translation engine to handle translating bot messages and other things,
 * independent of Minecraft
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class TranslationManager {

    public static final TranslationManager INSTANCE = new TranslationManager();

    private Map<String, String> translations = Collections.emptyMap();
    private final Type type = new TypeToken<Map<String, String>>() {}.getType();
    private final Gson gson = new Gson();

    /**
     * Load a translation file
     *
     * @param lang The Language Code to load translations for
     */
    public void loadTranslations(String lang) {
        translations.clear();
        loadTranslatedFile(lang);
    }

    /**
     * Internal method to load translations from files.
     *
     * @param lang The Language Code to load translations for
     */
    private void loadTranslatedFile(String lang) {
        File f = new File("config/simple-discord-link/language/" + lang + ".json");
        f.getParentFile().mkdirs();

        if (f.exists()) {
            if (loadFromFile(f)) {
                return;
            }
        } else if (lang.equals("en_us")) {
            if (createFileFromResource(f, "assets/lang/en_us.json")) {
                loadFromFile(f);
                return;
            }
        }

        if (loadFromResource("assets/lang/" + lang + ".json")) {
            return;
        }

        BotController.INSTANCE.getLogger().warn("Failed to load translation for {}. Falling back to en_us.", lang);
        loadFromResource("assets/lang/en_us.json");
    }

    /**
     * Load and parse a JSON file into a usable language map
     *
     * @param file The File to load from the disk
     * @return True if loaded, false if the file does not exist or failed to load
     */
    private boolean loadFromFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            translations = gson.fromJson(reader, type);
            return true;
        } catch (IOException e) {
            BotController.INSTANCE.getLogger().error("Failed to load translation file: {}", file.getAbsolutePath(), e);
        }
        return false;
    }

    /**
     * Load and parse a JSON file from the Jar resources
     *
     * @param resourcePath The path in the jar to load the resource from
     * @return True if loaded, false if the file does not exist or failed to load
     */
    private boolean loadFromResource(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null)
                return false;

            InputStreamReader reader = new InputStreamReader(inputStream);
            translations = gson.fromJson(reader, type);
            return true;
        } catch (IOException e) {
            BotController.INSTANCE.getLogger().error("Failed to load resource translation file: {}", resourcePath, e);
        }
        return false;
    }

    private boolean createFileFromResource(File targetFile, String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                BotController.INSTANCE.getLogger().error("Default translation file {} not found in resources!", resourcePath);
                return false;
            }

            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            BotController.INSTANCE.getLogger().info("Default translation file created at {}", targetFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            BotController.INSTANCE.getLogger().error("Failed to create default translation file {}", targetFile.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Try to convert a language key into human-readable text
     *
     * @param key The translation key to resolve
     * @return The parsed text, or the translation key if the value does not exist
     */
    public String translate(String key) {
        return translations.getOrDefault(key, key);
    }

}
