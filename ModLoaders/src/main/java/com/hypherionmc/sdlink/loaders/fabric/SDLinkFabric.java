package com.hypherionmc.sdlink.loaders.fabric;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.compat.MModeCompat;
import com.hypherionmc.sdlink.compat.rolesync.RoleSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.FTBRankSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.LuckPermsSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.PlayerRolesSync;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.api.DedicatedServerModInitializer;

public final class SDLinkFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ServerEvents events = ServerEvents.getInstance();
        CraterEventBus.INSTANCE.registerEventListener(events);
        SDLinkNetworking.registerPackets();

        if (ModloaderEnvironment.INSTANCE.isModLoaded("mmode")) {
            MModeCompat.init();
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("ftbranks")) {
            CraterEventBus.INSTANCE.registerEventListener(FTBRankSync.INSTANCE);
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("luckperms")) {
            CraterEventBus.INSTANCE.registerEventListener(LuckPermsSync.INSTANCE);
        }

        if (ModloaderEnvironment.INSTANCE.isModLoaded("player_roles")) {
            CraterEventBus.INSTANCE.registerEventListener(PlayerRolesSync.INSTANCE);
        }
    }
}
