package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.util.ServiceUtil;
import com.hypherionmc.sdlink.core.messaging.Result;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface SDLinkMCPlatform {

    public final SDLinkMCPlatform INSTANCE = ServiceUtil.load(SDLinkMCPlatform.class);

    public Result executeCommand(MinecraftServer server, String command);
    public boolean isDevEnv();
    public String getPlayerSkinUUID(ServerPlayer player);

}
