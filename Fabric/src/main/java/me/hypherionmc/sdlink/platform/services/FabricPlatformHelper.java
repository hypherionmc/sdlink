package me.hypherionmc.sdlink.platform.services;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import me.hypherionmc.sdlink.SDLinkFakePlayer;
import me.hypherionmc.sdlink.SafeCalls;
import me.hypherionmc.sdlinklib.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements ModHelper {

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
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getPlayerSkinUUID(ServerPlayer player) {
        if (player == null)
            return "server";

        if (ModloaderEnvironment.INSTANCE.isModLoaded("fabrictailor")) {
            return SafeCalls.getTailorSkin(player);
        }

        return player.getStringUUID();
    }

    @Override
    public boolean isModLoaded(String mod) {
        return FabricLoader.getInstance().isModLoaded(mod);
    }
}
