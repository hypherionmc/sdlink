package com.hypherionmc.sdlink.client;

import com.hypherionmc.sdlink.networking.SDLinkNetworking;

import java.util.HashMap;

/**
 * @author HypherionSA
 * Client Side Functions (Mostly for mentioning Stuff)
 */
public class ClientEvents {

    public static HashMap<String, String> roles = new HashMap<>();
    public static HashMap<String, String> channels = new HashMap<>();
    public static HashMap<String, String> users = new HashMap<>();

    public static void init() {
        SDLinkNetworking.registerPackets();
    }

}
