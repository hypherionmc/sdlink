package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SDLinkFabric;
import net.minecraft.network.chat.PlayerChatMessage;
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

    @Inject(
            method = "broadcastChatMessage",
            at = @At(
                    value = "HEAD")
    )
    public void onGameMessage(PlayerChatMessage playerChatMessage, CallbackInfo ci) {
        String filteredText = playerChatMessage.serverContent().getString();
        if (!filteredText.startsWith("/")) {
            SDLinkFabric.serverEvents.onServerChatEvent(
                    filteredText,
                    player.getDisplayName().getString(),
                    player.getUUID()
            );
        }
    }
}
