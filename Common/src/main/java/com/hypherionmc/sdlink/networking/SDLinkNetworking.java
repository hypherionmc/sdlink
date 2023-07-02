package com.hypherionmc.sdlink.networking;

import com.hypherionmc.craterlib.core.network.CraterNetworkHandler;
import com.hypherionmc.craterlib.core.network.PacketDirection;
import com.hypherionmc.craterlib.core.platform.CommonPlatform;
import com.hypherionmc.sdlink.SDLinkConstants;

/**
 * @author HypherionSA
 * Network Controller
 */
public class SDLinkNetworking {

    public static final CraterNetworkHandler networkHandler = CommonPlatform.INSTANCE.createPacketHandler(SDLinkConstants.MOD_ID);

    public static void registerPackets() {
        networkHandler.registerPacket(MentionsSyncPacket.class, MentionsSyncPacket::new, PacketDirection.TO_CLIENT);
    }

}
