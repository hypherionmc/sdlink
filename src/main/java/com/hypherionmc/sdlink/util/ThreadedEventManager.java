/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.core.discord.BotController;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author HypherionSA
 * Run discord events in seperate threads
 */
public class ThreadedEventManager extends InterfacedEventManager {

    @Override
    public void handle(@NotNull GenericEvent event) {
        if (BotController.taskManager.isShutdown() || BotController.taskManager.isTerminated())
            return;

        CompletableFuture.runAsync(() -> super.handle(event), BotController.taskManager);
    }
}
