package com.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.abstraction.server.AbstractServer;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.compat.FabricTailor;
import com.hypherionmc.sdlink.compat.Vanish;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements SDLinkMCPlatform {

    @Override
    public Result executeCommand(String command, int permLevel, MessageReceivedEvent event, String member) {
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server, permLevel, member, event);

        try {
            AbstractServer.executeCommand(server, fakePlayer, command);
            return Result.success("Command sent to server");
        } catch (Exception e) {
            fakePlayer.sendFailure(Component.literal(e.getMessage()));
            return Result.error(e.getMessage());
        }
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getPlayerSkinUUID(ServerPlayer player) {
        if (ModloaderEnvironment.INSTANCE.isModLoaded("fabrictailor")) {
            return FabricTailor.getTailorSkin(player);
        }

        return player.getStringUUID();
    }

    @Override
    public boolean playerIsActive(ServerPlayer player) {
        if (ModloaderEnvironment.INSTANCE.isModLoaded("melius-vanish")) {
            return !Vanish.isPlayerVanished(player);
        }

        return true;
    }
}
