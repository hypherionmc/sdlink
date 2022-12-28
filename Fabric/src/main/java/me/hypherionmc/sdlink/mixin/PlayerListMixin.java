package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void onPlayerJoin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        ServerEvents.getInstance().playerJoinEvent(serverPlayer);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayer serverPlayer, CallbackInfo ci) {
        ServerEvents.getInstance().playerLeaveEvent(serverPlayer);
    }

}
