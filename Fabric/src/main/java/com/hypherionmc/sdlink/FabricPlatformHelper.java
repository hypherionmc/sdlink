package com.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements SDLinkMCPlatform {

    @Override
    public void executeCommand(String command, int permLevel, MessageReceivedEvent event, String member) {
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server, permLevel, member, event);

        try {
            server.getCommands().performCommand(fakePlayer, command);
        } catch (Exception e) {
            fakePlayer.sendFailure(new TextComponent(e.getMessage()));
        }
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
