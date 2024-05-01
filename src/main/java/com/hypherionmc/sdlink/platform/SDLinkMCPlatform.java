package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.core.platform.CompatUtils;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.craterlib.nojang.world.entity.player.BridgedPlayer;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import shadow.kyori.adventure.text.Component;

public class SDLinkMCPlatform {

    public static final SDLinkMCPlatform INSTANCE = new SDLinkMCPlatform();

    public Result executeCommand(String command, int permLevel, MessageReceivedEvent event, String member) {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server, permLevel, member, event);

        try {
            server.executeCommand(server, fakePlayer, command);
            return Result.success("Command sent to server");
        } catch (Exception e) {
            fakePlayer.onError(Component.text(e.getMessage()));
            return Result.error(e.getMessage());
        }
    }

    public boolean isDevEnv() {
        return ModloaderEnvironment.INSTANCE.isDevEnv();
    }

    public String getPlayerSkinUUID(BridgedPlayer player) {
        return CompatUtils.INSTANCE.getSkinUUID(player);
    }

    public boolean playerIsActive(BridgedPlayer player) {
        return CompatUtils.INSTANCE.isPlayerActive(player);
    }
}
