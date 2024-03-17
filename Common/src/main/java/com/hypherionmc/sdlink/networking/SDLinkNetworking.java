package com.hypherionmc.sdlink.networking;

import com.hypherionmc.craterlib.core.networking.CraterPacketNetwork;

/**
 * @author HypherionSA
 * Network Controller
 */
public class SDLinkNetworking {

    public static void registerPackets() {
        CraterPacketNetwork.registerPacket(
                MentionsSyncPacket.CHANNEL,
                MentionsSyncPacket.class,
                MentionsSyncPacket::write,
                MentionsSyncPacket::decode,
                MentionsSyncPacket::handle
        );
    }

}
