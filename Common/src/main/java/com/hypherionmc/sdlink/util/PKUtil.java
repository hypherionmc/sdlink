package com.hypherionmc.sdlink.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PKUtil {
    public static boolean isPK(String id) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.pluralkit.me/v2/messages/" + id).openConnection();
            con.setRequestMethod("GET");
            return con.getResponseCode() == 200;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
