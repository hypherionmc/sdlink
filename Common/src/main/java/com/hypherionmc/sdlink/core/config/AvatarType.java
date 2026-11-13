/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config;

/**
 * @author HypherionSA
 * The type of User Icon/Avatar that will be used for Discord Messages
 */
public enum AvatarType {
    AVATAR("https://skinatar.firstdark.dev/avatar/{uuid}"),
    HEAD("https://skinatar.firstdark.dev/isometric/{uuid}"),
    BODY("https://skinatar.firstdark.dev/body/{uuid}"),
    COMBO("https://skinatar.firstdark.dev/avatar/{uuid}"),
    CUSTOM("");

    private final String url;

    AvatarType(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return this.url;
    }

    public String resolve(String uuid, String username) {
        if (this == CUSTOM) {
            return SDLinkConfig.INSTANCE.chatConfig.customAvatarService
                    .replace("{uuid}", uuid)
                    .replace("{username}", username);
        }

        return this.url.replace("{uuid}", uuid);
    }
}
