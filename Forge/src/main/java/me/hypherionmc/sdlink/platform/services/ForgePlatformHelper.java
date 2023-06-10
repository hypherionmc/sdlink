package me.hypherionmc.sdlink.platform.services;

import me.hypherionmc.sdlink.SDLinkFakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements ModHelper {

    @Override
    public void executeCommand(MinecraftServer server, String command) {
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server);
        if (fakePlayer.hasPermission(4)) {
            try {
                server.getCommands().getDispatcher().execute(command, fakePlayer);
            } catch (Exception e) {
                fakePlayer.sendFailure(Component.literal(e.getMessage()));
            }
        } else {
            fakePlayer.sendFailure(Component.literal("SDLinkFakePlayer does not have permission to execute this command. Please make sure the user is OPPED"));
        }
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
