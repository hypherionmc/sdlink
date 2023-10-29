package com.hypherionmc.sdlink.client;

import com.hypherionmc.sdlink.networking.SDLinkNetworking;

/**
 * @author HypherionSA
 * Client Side Functions (Mostly for mentioning Stuff)
 */
public class ClientEvents {
    public static boolean mentionsEnabled = false;

    public static void init() {
        SDLinkNetworking.registerPackets();
    }

}
