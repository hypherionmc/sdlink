package me.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class SDLinkFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ServerEvents events = ServerEvents.getInstance();
        CraterEventBus.INSTANCE.registerEventListener(events);
    }
}
