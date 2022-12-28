package me.hypherionmc.sdlink.platform.services;

import me.hypherionmc.sdlink.SDLinkFakePlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class ForgePlatformHelper {

    public static void executeCommand(MinecraftServer server, String command) {
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server);
        if (fakePlayer.hasPermissions(4)) {
            try {
                server.getCommands().getDispatcher().execute(command, fakePlayer.createCommandSourceStack());
            } catch (Exception e) {
                fakePlayer.displayClientMessage(new StringTextComponent(e.getMessage()), false);
            }
        } else {
            fakePlayer.displayClientMessage(new StringTextComponent("SDLinkFakePlayer does not have permission to execute this command. Please make sure the user is OPPED"), false);
        }
    }
}
