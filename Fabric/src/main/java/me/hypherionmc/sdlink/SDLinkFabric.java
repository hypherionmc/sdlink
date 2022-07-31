package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class SDLinkFabric implements DedicatedServerModInitializer {

    public static ServerEvents serverEvents;

    @Override
    public void onInitializeServer() {
        serverEvents = ServerEvents.getInstance();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> serverEvents.onCommandRegister(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STARTING.register(serverEvents::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> serverEvents.onServerStarted());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> serverEvents.onServerStopping());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverEvents.onServerStoppedEvent());
    }
}
