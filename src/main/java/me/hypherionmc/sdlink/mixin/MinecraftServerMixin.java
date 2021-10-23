package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SimpleDiscordLink;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(at = @At("HEAD"), method = "onServerCrash", remap = false)
    public void onServerCrash(CallbackInfo info) {
        if (SimpleDiscordLink.serverEvents != null) {
            SimpleDiscordLink.serverEvents.onServerCrashed();
        }
    }
}
