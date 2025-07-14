package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.core.discord.BotController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PKUtil {
    public static boolean isPK(String id) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.pluralkit.me/v2/messages/" + id).openConnection();
            con.setRequestMethod("GET");
            // Set a one-second timeout to avoid indefinitely blocking the thread
            con.setConnectTimeout(1_000);
            return con.getResponseCode() == 200;
        } catch (IOException e) {
            BotController.INSTANCE.getLogger().error("An error occurred while checking message in PluralKit api", e);
            return false;
        }
    }
}
