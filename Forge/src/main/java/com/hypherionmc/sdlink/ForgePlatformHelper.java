package com.hypherionmc.sdlink;

import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements SDLinkMCPlatform {

    @Override
    public Result executeCommand(MinecraftServer server, String command) {
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server);
        if (fakePlayer.hasPermission(4)) {
            try {
                server.getCommands().getDispatcher().execute(command, fakePlayer);
                return Result.success("Command Executed");
            } catch (Exception e) {
                fakePlayer.sendFailure(Component.literal(e.getMessage()));
            }
        } else {
            fakePlayer.sendFailure(Component.literal("SDLinkFakePlayer does not have permission to execute this command. Please make sure the user is OPPED"));
        }

        return Result.error("Failed to execute command. Check your server logs");
    }

    @Override
    public boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    @Override
    public String getPlayerSkinUUID(ServerPlayer player) {
        return player.getStringUUID();
    }
}
