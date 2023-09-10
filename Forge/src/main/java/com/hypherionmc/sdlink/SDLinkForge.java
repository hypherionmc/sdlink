package com.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.compat.Vanish;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(SDLinkConstants.MOD_ID)
public class SDLinkForge {

    public SDLinkForge() {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            SDLinkNetworking.registerPackets();
            ServerEvents events = ServerEvents.getInstance();
            CraterEventBus.INSTANCE.registerEventListener(events);

            if (ModloaderEnvironment.INSTANCE.isModLoaded("vmod")) {
                MinecraftForge.EVENT_BUS.register(new Vanish());
            }
        });

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientEvents::init);
    }
}
