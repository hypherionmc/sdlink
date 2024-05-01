/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.util;

import com.hypherionmc.sdlink.core.discord.BotController;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author HypherionSA
 * Run discord events in seperate threads
 */
public class ThreadedEventManager extends InterfacedEventManager {

    @Override
    public void handle(@NotNull GenericEvent event) {
        BotController.taskManager.submit(() -> super.handle(event));
    }
}
