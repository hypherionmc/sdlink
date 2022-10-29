package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class SDLinkFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> ServerEvents.getInstance().onCommandRegister(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STARTING.register(ServerEvents.getInstance()::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ServerEvents.getInstance().onServerStarted());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerEvents.getInstance().onServerStopping());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerEvents.getInstance().onServerStoppedEvent());
    }
}
