package com.hypherionmc.sdlink.loaders.neoforge;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SDLinkConstants.MOD_ID)
public class SDLinkNeoForge {

    public SDLinkNeoForge(IEventBus bus) {
        SDLinkNetworking.registerPackets();

        if (ModloaderEnvironment.INSTANCE.getEnvironment().isServer()) {
            ServerEvents events = ServerEvents.getInstance();
            CraterEventBus.INSTANCE.registerEventListener(events);
        }

        if (ModloaderEnvironment.INSTANCE.getEnvironment().isClient()) {
            ClientEvents.init();
        }
    }
}
