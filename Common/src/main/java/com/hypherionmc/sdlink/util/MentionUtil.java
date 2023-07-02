package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.managers.CacheManager;

/**
 * @author HypherionSA
 * Helper class to convert mentions from Minecraft Chat into Discord Format
 */
public class MentionUtil {

    public static String parse(String message) {
        String finalMessage = message;

        try {
            if (finalMessage.contains("[#")) {
                String msg = message.substring(message.indexOf("[#") + 1);
                msg = msg.substring(0, msg.indexOf("]"));

                if (!CacheManager.getServerChannels().isEmpty() && CacheManager.getServerChannels().containsKey(msg)) {
                    finalMessage = finalMessage.replace("[" + msg + "]", CacheManager.getServerChannels().get(msg));
                }
            }

            if (finalMessage.contains("[@")) {
                String msg = finalMessage.substring(finalMessage.indexOf("[@") + 1);
                msg = msg.substring(0, msg.indexOf("]"));

                if (!CacheManager.getServerRoles().isEmpty() && CacheManager.getServerRoles().containsKey(msg)) {
                    finalMessage = finalMessage.replace("[" + msg + "]", CacheManager.getServerRoles().get(msg));
                }

                if (!CacheManager.getUserCache().isEmpty() && CacheManager.getUserCache().containsKey(msg)) {
                    finalMessage = finalMessage.replace("[" + msg + "]", CacheManager.getUserCache().get(msg));
                }
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to parse mention", e);
            }
        }

        return finalMessage;
    }

}
