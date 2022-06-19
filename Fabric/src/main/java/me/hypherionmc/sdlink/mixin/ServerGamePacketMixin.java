package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SDLinkFabric;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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

    @Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Lnet/minecraft/server/network/FilteredText;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ChatDecorator;decorateChat(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/FilteredText;Lnet/minecraft/network/chat/MessageSignature;Z)Ljava/util/concurrent/CompletableFuture;", shift = At.Shift.BEFORE))
    public void onGameMessage(ServerboundChatPacket serverboundChatPacket, FilteredText<String> filteredText, CallbackInfo ci) {
        if (!filteredText.raw().startsWith("/")) {
            SDLinkFabric.serverEvents.onServerChatEvent(filteredText.raw(), player.getDisplayName().getString(), player.getUUID());
        }
    }

}
