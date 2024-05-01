/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.events;

import com.hypherionmc.craterlib.core.event.CraterEvent;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author HypherionSA
 * Events that get triggered when the bot verification list changes
 */
public class VerificationEvent {

    @Getter
    @RequiredArgsConstructor
    public static class PlayerVerified extends CraterEvent {
        private final MinecraftAccount account;
    }

    @Getter
    @RequiredArgsConstructor
    public static class PlayerUnverified extends CraterEvent {
        private final MinecraftAccount account;
    }

}
