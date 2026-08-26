/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.messaging;

/**
 * @author HypherionSA
 * Used to specify the type of message being sent
 */
public enum MessageType {
    CHAT,
    START,
    STARTED,
    STOP,
    STOPPED,
    JOIN,
    LEAVE,
    ADVANCEMENTS,
    DEATH,
    COMMANDS,
    CONSOLE,
    WHITELIST,
    WHITELIST_REMOVE,
    CUSTOM,
    RELAY
}
