package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import net.minecraft.network.chat.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HypherionSA
 * Helper class to convert mentions from Minecraft Chat into Discord Format
 */
public class SDLinkChatUtils {

    private static final Pattern CHANNEL_PATTERN = Pattern.compile("\\[#(.*?)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern USER_ROLE_PATTERN = Pattern.compile("\\[@(.*?)\\]", Pattern.CASE_INSENSITIVE);

    public static String parse(String message) {
        String finalMessage = message;

        try {
            Matcher m = CHANNEL_PATTERN.matcher(message);

            while (m.find()) {
                String channelKey = m.group().replace("[", "").replace("]", "");

                if (!CacheManager.getServerChannels().isEmpty() && CacheManager.getServerChannels().containsKey(channelKey)) {
                    finalMessage = finalMessage.replace("[" + channelKey + "]", CacheManager.getServerChannels().get(channelKey));
                }
            }

            Matcher c = USER_ROLE_PATTERN.matcher(message);

            while (c.find()) {
                String key = c.group().replace("[", "").replace("]", "");

                if (!CacheManager.getServerRoles().isEmpty() && CacheManager.getServerRoles().containsKey(key)) {
                    finalMessage = finalMessage.replace("[" + key + "]", CacheManager.getServerRoles().get(key));
                }

                if (!CacheManager.getUserCache().isEmpty() && CacheManager.getUserCache().containsKey(key)) {
                    finalMessage = finalMessage.replace("[" + key + "]", CacheManager.getUserCache().get(key));
                }
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to parse mention", e);
            }
        }

        return finalMessage;
    }

    public static MutableComponent parseChatLinks(String input) {
        Pattern pattern = Pattern.compile("\\b(?:https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]");
        Matcher matcher = pattern.matcher(input);

        MutableComponent component = Component.empty();

        int lastEnd = 0;
        while (matcher.find()) {
            String url = matcher.group();
            String msg = input.substring(lastEnd, matcher.start());

            component.append(ModUtils.resolve(msg));

            Style emptyStyle = Style.EMPTY;
            emptyStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            emptyStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Open")));

            MutableComponent urlComponent = Component.literal(url).withStyle(emptyStyle);
            component.append(urlComponent);
            lastEnd = matcher.end();
        }

        String remaining = input.substring(lastEnd);
        component.append(ModUtils.resolve(remaining));

        return component;
    }

}
