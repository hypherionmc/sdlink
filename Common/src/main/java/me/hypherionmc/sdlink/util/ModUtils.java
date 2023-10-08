package me.hypherionmc.sdlink.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ModUtils {

    // Some text components contain duplicate text, resulting in duplicate messages
    // sent back to discord. This should help fix those issues
    public static Component safeCopy(Component inComponent) {
        return Component.literal(inComponent.getString()).withStyle(inComponent.getStyle());
    }

    public static String strip(String inString, String... toStrip) {
        String finalString = inString;

        for (String strip : toStrip) {
            if (finalString.startsWith(strip))
                finalString = finalString.replaceFirst(strip, "");

            if (finalString.startsWith(" "))
                finalString = finalString.replaceFirst(" ", "");
        }

        return finalString;
    }

}
