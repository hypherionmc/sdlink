/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class SystemUtils {

    /**
     * Convert Bytes into a human-readable format, like 1GB
     * From https://stackoverflow.com/a/3758880
     *
     * @param bytes The Size in Bytes
     * @return The size formatted in KB, MB, GB, TB, PB etc
     */
    public static String byteToHuman(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    // Time Conversion

    /**
     * Convert Seconds into a Timestamp
     *
     * @param sec Input in seconds
     */
    public static String secondsToTimestamp(long sec) {
        long seconds = sec % 60;
        long minutes = (sec / 60) % 60;
        long hours = (sec / 3600) % 24;
        long days = sec / (3600 * 24);

        String timeString = String.format("%02d hour(s), %02d minute(s), %02d second(s)", hours, minutes, seconds);

        if (days > 0) {
            timeString = String.format("%d day(s), %s", days, timeString);
        }

        return timeString;
    }

    public static boolean isLong(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }
}
