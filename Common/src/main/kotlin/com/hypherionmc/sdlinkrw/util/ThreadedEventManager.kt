package com.hypherionmc.sdlinkrw.util

import com.hypherionmc.sdlink.core.discord.BotController
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.InterfacedEventManager
import java.util.concurrent.CompletableFuture

class ThreadedEventManager: InterfacedEventManager() {

    override fun handle(event: GenericEvent) {
        try {
            if (BotController.INSTANCE.taskManager.isShutdown || BotController.INSTANCE.taskManager.isTerminated) return

            CompletableFuture.runAsync({ super.handle(event) }, BotController.INSTANCE.taskManager)
        } catch (_: Exception) {}
    }
}