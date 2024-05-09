package com.hypherionmc.sdlink.loaders.fabric;

import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import net.fabricmc.api.ClientModInitializer;

/**
 * @author HypherionSA
 * Client Initializer
 */
public class SDLinkFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientEvents.init();
        SDLinkNetworking.registerPackets();
    }
}
