package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SDLinkFabric;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Shadow public ServerGamePacketListenerImpl connection;

    @Inject(at = @At("HEAD"), method = "die")
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        SDLinkFabric.serverEvents.onPlayerDeath(
                connection.getPlayer(),
                damageSource.getLocalizedDeathMessage(connection.player).getString()
        );
    }

}
