package com.hypherionmc.sdlink;

import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements SDLinkMCPlatform {

    @Override
    public void executeCommand(String command, int permLevel, MessageReceivedEvent event, String member) {
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        SDLinkFakePlayer fakePlayer = new SDLinkFakePlayer(server, permLevel, member, event);

        try {
            server.getCommands().performPrefixedCommand(fakePlayer, command);
        } catch (Exception e) {
            fakePlayer.sendFailure(Component.literal(e.getMessage()));
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
}
