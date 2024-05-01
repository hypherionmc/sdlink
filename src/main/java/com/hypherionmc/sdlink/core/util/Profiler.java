/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.util;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;

/**
 * @author HypherionSA
 * Profiler to time how long certain tasks take to execute
 * Used to find bottlenecks in some systems
 */
//TODO Remove this on release
public class Profiler {

    private final String profilerName;
    private long startTime;
    private boolean hasStarted = false;
    private String message = "";

    private Profiler(String profilerName) {
        this.profilerName = profilerName;
        this.hasStarted = false;
    }

    public static Profiler getProfiler(String name) {
        return new Profiler(name);
    }

    public void start(String message) {
        if (!SDLinkConfig.INSTANCE.generalConfig.debugging)
            return;

        this.message = message;
        this.startTime = System.nanoTime();
        this.hasStarted = true;
    }

    public void stop() {
        if (!SDLinkConfig.INSTANCE.generalConfig.debugging)
            return;

        if (!hasStarted) {
            BotController.INSTANCE.getLogger().error("[Profiler (" + this.profilerName + ")] was not started");
            return;
        }

        long stopTime = System.nanoTime();
        double seconds = (double) (stopTime - startTime) / 1_000_000;

        BotController.INSTANCE.getLogger().info("[Profiler (" + this.profilerName + ")] " + message + " took " + seconds + " ms");
        hasStarted = false;
    }

}
