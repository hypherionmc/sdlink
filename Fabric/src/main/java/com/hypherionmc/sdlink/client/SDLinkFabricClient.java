package com.hypherionmc.sdlink.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * @author HypherionSA
 * Client Initializer
 */
public class SDLinkFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientEvents.init();
    }
}
