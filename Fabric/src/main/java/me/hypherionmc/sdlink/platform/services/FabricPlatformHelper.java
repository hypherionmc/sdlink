package me.hypherionmc.sdlink.platform.services;

import me.hypherionmc.sdlink.SDLinkFakePlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;

public class FabricPlatformHelper {

    public static void executeCommand(MinecraftServer server, String command) {
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server);
        if (fakePlayer.hasPermission(4)) {
            try {
                server.getCommands().getDispatcher().execute(command, fakePlayer);
            } catch (Exception e) {
                fakePlayer.sendFailure(new TextComponent(e.getMessage()));
            }
        } else {
            fakePlayer.sendFailure(new TextComponent("SDLinkFakePlayer does not have permission to execute this command. Please make sure the user is OPPED"));
        }
    }

}
