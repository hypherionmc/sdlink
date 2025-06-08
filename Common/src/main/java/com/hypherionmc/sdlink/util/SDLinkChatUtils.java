package com.hypherionmc.sdlink.util;

import com.hypherionmc.craterlib.utils.ChatUtils;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageIgnoreConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import shadow.kyori.adventure.text.Component;
import shadow.kyori.adventure.text.event.ClickEvent;
import shadow.kyori.adventure.text.event.HoverEvent;
import shadow.kyori.adventure.text.format.NamedTextColor;
import shadow.kyori.adventure.text.format.Style;
import shadow.kyori.adventure.text.minimessage.MiniMessage;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HypherionSA
 * Helper class to convert mentions from Minecraft Chat into Discord Format
 */
public final class SDLinkChatUtils {

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

    public static Component parseChatLinks(String input) {
        Pattern pattern = Pattern.compile("\\b(?:https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]");
        Matcher matcher = pattern.matcher(input);

        Component component = Component.empty();

        int lastEnd = 0;
        while (matcher.find()) {
            String url = matcher.group();
            String msg = input.substring(lastEnd, matcher.start());

            component = component.append(ChatUtils.resolve(msg, SDLinkConfig.INSTANCE.chatConfig.formatting));

            Style emptyStyle = Style.empty()
                    .color(NamedTextColor.BLUE)
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to Open")));

            Component urlComponent = Component.text(url).style(emptyStyle);
            component = component.append(urlComponent);
            lastEnd = matcher.end();
        }

        String remaining = input.substring(lastEnd);
        component = component.append(ChatUtils.resolve(remaining, SDLinkConfig.INSTANCE.chatConfig.formatting));

        return component;
    }

    public static String applyFiltering(String input, Predicate<MessageIgnoreConfig.Ignore> ignoreCheck) {
        return applyFiltering(input, ignoreCheck, (i) -> false);
    }

    public static String applyFiltering(String input, Predicate<MessageIgnoreConfig.Ignore> ignoreCheck, Predicate<MessageIgnoreConfig.Ignore> applyIgnoreIfConsole) {
        if (!SDLinkConfig.INSTANCE.ignoreConfig.enabled)
            return input;

        for (MessageIgnoreConfig.Ignore i : SDLinkConfig.INSTANCE.ignoreConfig.entries) {
            if (!ignoreCheck.test(i))
                continue;

            boolean isMatch = false;

            switch (i.searchMode) {
                case MATCHES:
                    isMatch = input.equalsIgnoreCase(i.search);
                    break;

                case CONTAINS:
                    isMatch = input.contains(i.search);
                    break;

                case STARTS_WITH:
                    isMatch = input.startsWith(i.search);
                    break;

                case REGEX:
                    try {
                        Pattern pattern = Pattern.compile(i.search);
                        Matcher matcher = pattern.matcher(input);
                        isMatch = matcher.find();
                    } catch (Exception e) {
                        BotController.INSTANCE.getLogger().error("Invalid regex pattern: {}", i.search);
                    }
                    break;
            }

            if (isMatch) {
                if (applyIgnoreIfConsole.test(i)) {
                    return input;
                }

                if (i.action == MessageIgnoreConfig.ActionMode.REPLACE) {
                    input = (i.searchMode == MessageIgnoreConfig.FilterMode.REGEX)
                            ? input.replaceAll(i.search, i.replace)
                            : input.replace(i.search, i.replace);
                } else {
                    input = "";
                }
            }
        }

        return input;
    }

    public static shadow.kyori.adventure.text.Component format(String value) {
        value = convertFormattingCodes(value);

        try {
            return MiniMessage.miniMessage().deserializeOr(value, shadow.kyori.adventure.text.Component.text(value));
        } catch (Exception var2) {
            return shadow.kyori.adventure.text.Component.text(value);
        }
    }

    private static String convertFormattingCodes(String input) {
        return input.replaceAll("§([0-9a-fklmnor])", "§$1");
    }

}
