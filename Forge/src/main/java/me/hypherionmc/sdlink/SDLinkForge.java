package me.hypherionmc.sdlink;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(SDLinkConstants.MOD_ID)
public class SDLinkForge {

    public SDLinkForge() {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            ServerEvents events = ServerEvents.getInstance();
            CraterEventBus.INSTANCE.registerEventListener(events);
        });
    }
}
