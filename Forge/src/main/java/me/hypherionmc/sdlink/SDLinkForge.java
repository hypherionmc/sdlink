package me.hypherionmc.sdlink;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

@Mod(SDLinkConstants.MOD_ID)
public class SDLinkForge {

    public SDLinkForge() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> "", (a, b) -> true));
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> MinecraftForge.EVENT_BUS.register(new ForgeEventHandler()));
    }
}
