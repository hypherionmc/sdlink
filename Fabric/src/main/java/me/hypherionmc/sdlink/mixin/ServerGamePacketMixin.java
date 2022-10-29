package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SDLinkFabric;
import me.hypherionmc.sdlink.SafeCalls;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketMixin {

    @Shadow public ServerPlayer player;

    @Inject(
            method = "handleChat(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V",
                    shift = At.Shift.BEFORE
            )
    )
    public void onGameMessage(String string, CallbackInfo ci) {
        if (!string.startsWith("/")) {
            if (FabricLoader.getInstance().isModLoaded("fabrictailor")) {
                SafeCalls.tailerPlayerMessage(player, string);
            } else {
                ServerEvents.getInstance().onServerChatEvent(
                        string,
                        player.getDisplayName().getString(),
                        player.getUUID().toString()
                );
            }
        }
    }

}
