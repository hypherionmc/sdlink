package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.events.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("sdlink")
public class SimpleDiscordLink {

    public static ServerEvents serverEvents;

    public SimpleDiscordLink() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (a, b) -> true));
        if (FMLEnvironment.dist != Dist.CLIENT) {
            serverEvents = new ServerEvents();
            MinecraftForge.EVENT_BUS.register(serverEvents);
        }
    }

}
