package com.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.abstraction.server.AbstractServer;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLLoader;
import redstonedubstep.mods.vanishmod.VanishUtil;

public class ForgePlatformHelper implements SDLinkMCPlatform {

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
        return !FMLLoader.isProduction();
    }

    @Override
    public String getPlayerSkinUUID(ServerPlayer player) {
        return player.getStringUUID();
    }

    @Override
    public boolean playerIsActive(ServerPlayer player) {
        if (ModloaderEnvironment.INSTANCE.isModLoaded("vmod")) {
            return !VanishUtil.isVanished(player);
        }

        return true;
    }
}
