package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.events.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("sdlink")
public class SimpleDiscordLink {

    public SimpleDiscordLink() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ServerEvents());
        }
    }

}
