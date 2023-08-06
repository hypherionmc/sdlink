package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import me.hypherionmc.mcdiscordformatter.discord.DiscordSerializer;
import me.hypherionmc.mcdiscordformatter.minecraft.MinecraftSerializer;
import net.minecraft.ChatFormatting;
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
            if (finalString.startsWith(strip))
                finalString = finalString.replaceFirst(strip, "");

            if (finalString.startsWith(" "))
                finalString = finalString.replaceFirst(" ", "");
        }

        return finalString;
    }

    public static String resolve(Component component) {
        String returnVal = ChatFormatting.stripFormatting(component.getString());
        if (SDLinkConfig.INSTANCE.chatConfig.formatting) {
            returnVal = DiscordSerializer.INSTANCE.serialize(safeCopy(component).copy());
        }

        return returnVal;
    }

    public static Component resolve(String component) {
        Component returnVal = Component.literal(component);
        if (SDLinkConfig.INSTANCE.chatConfig.formatting) {
            returnVal = MinecraftSerializer.INSTANCE.serialize(component);
        }

        return returnVal;
    }
}