package me.hypherionmc.sdlink.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class ModUtils {

    // Some text components contain duplicate text, resulting in duplicate messages
    // sent back to discord. This should help fix those issues
    public static ITextComponent safeCopy(ITextComponent inComponent) {
        String value = inComponent.getString();
        Style style = inComponent.getStyle();
        return new StringTextComponent(value).withStyle(style);
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
