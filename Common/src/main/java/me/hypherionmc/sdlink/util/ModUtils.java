package me.hypherionmc.sdlink.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class ModUtils {

    // Some text components contain duplicate text, resulting in duplicate messages
    // sent back to discord. This should help fix those issues
    public static Component safeCopy(Component inComponent) {
        String value = inComponent.getString();
        Style style = inComponent.getStyle();
        return new TextComponent(value).withStyle(style);
    }

    public static String strip(String inString, String... toStrip) {
        String finalString = inString;

        for (String strip : toStrip) {
            finalString = finalString.replace(strip + " ", "");
            finalString = finalString.replace(strip, "");
        }

        return finalString;
    }

}
