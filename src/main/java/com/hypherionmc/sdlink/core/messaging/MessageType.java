/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging;

/**
 * @author HypherionSA
 * Used to specify the type of message being sent
 */
public enum MessageType {
    CHAT,
    START,
    STOP,
    JOIN,
    LEAVE,
    ADVANCEMENTS,
    DEATH,
    COMMANDS,
    CONSOLE,
    CUSTOM
}
