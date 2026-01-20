package com.hypherionmc.sdlink.loaders.hytale;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.slf4j.LoggerFactory;

public class SDLinkHytale extends JavaPlugin {

    public SDLinkHytale(JavaPluginInit init) {
        super(init);
        LoggerFactory.getLogger("");
        ServerEvents events = ServerEvents.getInstance();
        CraterEventBus.INSTANCE.registerEventListener(events);
    }
}
