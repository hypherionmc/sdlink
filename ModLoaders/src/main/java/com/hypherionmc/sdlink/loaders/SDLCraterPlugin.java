package com.hypherionmc.sdlink.loaders;

import com.hypherionmc.craterlib.api.loader.CraterLoader;
import com.hypherionmc.craterlib.api.loader.plugins.entrypoints.CraterPlugin;
import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.compat.MModeCompat;
import com.hypherionmc.sdlink.compat.rolesync.impl.FTBRankSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.LuckPermsSync;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlink.util.Debugger;

public class SDLCraterPlugin implements CraterPlugin {

    @Override
    public void onLoadClient() {
        ClientEvents.init();
    }

    @Override
    public void onLoadServer() {
        ServerEvents events = ServerEvents.getInstance();
        CraterEventBus.INSTANCE.registerEventListener(events);
        SDLinkNetworking.registerPackets();

        // TODO: Disable this on release
        Debugger.setupSentry();

        if (CraterLoader.isModLoaded("mmode")) {
            MModeCompat.init();
        }

        if (CraterLoader.isModLoaded("ftbranks")) {
            CraterEventBus.INSTANCE.registerEventListener(FTBRankSync.INSTANCE);
        }

        if (CraterLoader.isModLoaded("luckperms")) {
            CraterEventBus.INSTANCE.registerEventListener(LuckPermsSync.INSTANCE);
        }
    }

    @Override
    public String getPluginId() {
        return "";
    }
}
