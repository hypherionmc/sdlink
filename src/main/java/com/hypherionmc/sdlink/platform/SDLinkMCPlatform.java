package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.core.platform.CompatUtils;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.craterlib.nojang.world.entity.player.BridgedPlayer;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import shadow.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class SDLinkMCPlatform {

    public static final SDLinkMCPlatform INSTANCE = new SDLinkMCPlatform();

    public void executeCommand(String command, int permLevel, String member, CompletableFuture<Result> replier) {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server, permLevel, member, replier);

        try {
            server.executeCommand(server, fakePlayer, command);
        } catch (Exception e) {
            fakePlayer.onError(Component.text(e.getMessage()));
        }
    }

    public boolean isDevEnv() {
        return ModloaderEnvironment.INSTANCE.isDevEnv();
    }

    public String getPlayerSkinUUID(BridgedPlayer player) {
        return CompatUtils.INSTANCE.getSkinUUID(player);
    }

    public boolean playerIsActive(BridgedPlayer player) {
        // FIXME This check is reversed inside craterlib by mistake. Once fixed, this has to be swapped back
        if (ModloaderEnvironment.INSTANCE.isModLoaded("melius-vanish") || ModloaderEnvironment.INSTANCE.isModLoaded("vmod"))
            return !CompatUtils.INSTANCE.isPlayerActive(player);

        return CompatUtils.INSTANCE.isPlayerActive(player);
    }
}
