package me.hypherionmc.sdlink.platform.services;

import com.hypherionmc.craterlib.util.ServiceUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ModHelper {

    public final ModHelper INSTANCE = ServiceUtil.load(ModHelper.class);

    public void executeCommand(MinecraftServer server, String command);
    public boolean isDevEnv();
    public String getPlayerSkinUUID(ServerPlayer player);

    boolean isModLoaded(String mod);
}
