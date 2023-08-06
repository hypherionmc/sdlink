package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.util.ServiceUtil;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.Member;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface SDLinkMCPlatform {

    public final SDLinkMCPlatform INSTANCE = ServiceUtil.load(SDLinkMCPlatform.class);

    public void executeCommand(String command, int permLevel, MessageReceivedEvent event, String member);
    public boolean isDevEnv();
    public String getPlayerSkinUUID(ServerPlayer player);

}
