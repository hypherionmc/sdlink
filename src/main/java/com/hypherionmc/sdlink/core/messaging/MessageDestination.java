/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging;

/**
 * @author HypherionSA
 * Specifies to what channel a message should be delivered
 */
public enum MessageDestination {
    CHAT,
    EVENT,
    CONSOLE;

    public boolean isChat() {
        return this == CHAT;
    }

    public boolean isEvent() {
        return this == EVENT;
    }

    public boolean isConsole() {
        return this == CONSOLE;
    }
}
