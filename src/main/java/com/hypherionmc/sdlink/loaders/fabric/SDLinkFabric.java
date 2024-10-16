package com.hypherionmc.sdlink.loaders.fabric;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.compat.MModeCompat;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.api.DedicatedServerModInitializer;

public class SDLinkFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ServerEvents events = ServerEvents.getInstance();
        CraterEventBus.INSTANCE.registerEventListener(events);
        SDLinkNetworking.registerPackets();

        if (ModloaderEnvironment.INSTANCE.isModLoaded("mmode")) {
            MModeCompat.init();
        }
    }
}
