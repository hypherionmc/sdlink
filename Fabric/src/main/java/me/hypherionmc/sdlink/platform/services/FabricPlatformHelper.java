package me.hypherionmc.sdlink.platform.services;

import me.hypherionmc.sdlink.SDLinkFakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

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
}
