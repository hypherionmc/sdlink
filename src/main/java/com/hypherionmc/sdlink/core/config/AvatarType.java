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
    AVATAR("https://mc-heads.net/avatar/{uuid}/512"),
    HEAD("https://mc-heads.net/head/{uuid}/512"),
    BODY("https://mc-heads.net/body/{uuid}"),
    COMBO("https://mc-heads.net/combo/{uuid}/512");

    private final String url;

    AvatarType(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return this.url;
    }

    public String resolve(String uuid) {
        return this.url.replace("{uuid}", uuid);
    }
}
