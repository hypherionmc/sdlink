/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import lombok.Getter;

import java.util.HashMap;

public final class CacheManager {

    @Getter
    public static final HashMap<MessageType, MessageChannelConfig.DestinationObject> messageDestinations = new HashMap<>();

    public static void reloadChannelConfigCache() {
        try {
            messageDestinations.clear();
            messageDestinations.put(MessageType.CHAT, SDLinkConfig.INSTANCE.messageDestinations.chat);
            messageDestinations.put(MessageType.START, SDLinkConfig.INSTANCE.messageDestinations.start);
            messageDestinations.put(MessageType.STOP, SDLinkConfig.INSTANCE.messageDestinations.stop);
            messageDestinations.put(MessageType.JOIN, SDLinkConfig.INSTANCE.messageDestinations.join);
            messageDestinations.put(MessageType.LEAVE, SDLinkConfig.INSTANCE.messageDestinations.leave);
            messageDestinations.put(MessageType.ADVANCEMENTS, SDLinkConfig.INSTANCE.messageDestinations.advancements);
            messageDestinations.put(MessageType.DEATH, SDLinkConfig.INSTANCE.messageDestinations.death);
            messageDestinations.put(MessageType.COMMANDS, SDLinkConfig.INSTANCE.messageDestinations.commands);
            messageDestinations.put(MessageType.WHITELIST, SDLinkConfig.INSTANCE.messageDestinations.whitelist);
            messageDestinations.put(MessageType.CUSTOM, SDLinkConfig.INSTANCE.messageDestinations.custom);
        } catch (Exception ignored) {}
    }

}
