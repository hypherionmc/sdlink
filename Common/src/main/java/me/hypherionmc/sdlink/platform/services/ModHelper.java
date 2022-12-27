package me.hypherionmc.sdlink.platform.services;

import net.minecraft.server.MinecraftServer;

public interface ModHelper {
    public void executeCommand(MinecraftServer server, String command);
}
