package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.api.game.commands.CraterFakePlayer;
import com.hypherionmc.craterlib.api.game.server.CraterGameServer;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.craterlib.api.game.world.entity.player.CraterPlayer;
import com.hypherionmc.craterlib.api.loader.CraterCompat;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.server.ServerEvents;

import java.util.concurrent.CompletableFuture;

public final class SDLinkMCPlatform {

    public static final SDLinkMCPlatform INSTANCE = new SDLinkMCPlatform();

    public void executeCommand(String command, int permLevel, String member, CompletableFuture<Result> replier) {
        CraterGameServer server = ServerEvents.getInstance().getMinecraftServer();
        CraterFakePlayer fakePlayer = SDLinkFakePlayer.create(server, permLevel, member, replier);

        try {
            server.executeCommand(server, fakePlayer, command);
        } catch (Exception e) {
            fakePlayer.onError(Text.literal(e.getMessage()));
        }
    }

    public String getPlayerSkinUUID(CraterPlayer player) {
        return CraterCompat.getSkinUUID(player);
    }

    public boolean playerIsActive(CraterPlayer player) {
        if (!SDLinkCompatConfig.INSTANCE.common.vanish) {
            return true;
        }

        return CraterCompat.isPlayerActive(player);
    }
}
