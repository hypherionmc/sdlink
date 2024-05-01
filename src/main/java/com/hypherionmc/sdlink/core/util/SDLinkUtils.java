/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.util;

public class SDLinkUtils {

    public static boolean isNullOrEmpty(String inString) {
        return inString == null || inString.isEmpty();
    }

    public static int intInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}
