package com.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements SDLinkMCPlatform {

    @Override
    public Result executeCommand(MinecraftServer server, String command) {
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server);
        if (fakePlayer.hasPermission(4)) {
            try {
                server.getCommands().getDispatcher().execute(command, fakePlayer);
                return Result.success("Command Executed");
            } catch (Exception e) {
                fakePlayer.sendFailure(new TextComponent(e.getMessage()));
            }
        } else {
            fakePlayer.sendFailure(new TextComponent("SDLinkFakePlayer does not have permission to execute this command. Please make sure the user is OPPED"));
        }

        return Result.error("Failed to execute command. Check your server logs");
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getPlayerSkinUUID(ServerPlayer player) {
        if (ModloaderEnvironment.INSTANCE.isModLoaded("fabrictailor")) {
            return SafeCalls.getTailorSkin(player);
        }

        return player.getStringUUID();
    }
}
