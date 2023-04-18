package me.hypherionmc.sdlink.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ModUtils {

    // Some text components contain duplicate text, resulting in duplicate messages
    // sent back to discord. This should help fix those issues
    public static Component safeCopy(Component inComponent) {
        String value = inComponent.getString();
        Style style = inComponent.getStyle();
        return Component.literal(value).withStyle(style);
    }

    public static String strip(String inString, String... toStrip) {
        String finalString = inString;

        for (String strip : toStrip) {
            finalString = finalString.replaceFirst(strip + " ", "");
            finalString = finalString.replaceFirst(strip, "");
        }

        return finalString;
    }

}
