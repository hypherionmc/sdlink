package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SafeCalls;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
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
        Component filteredText = playerChatMessage.serverContent();
        if (!filteredText.getString().startsWith("/")) {
            if (FabricLoader.getInstance().isModLoaded("fabrictailor")) {
                SafeCalls.tailerPlayerMessage(player, filteredText);
            } else {
                ServerEvents.getInstance().onServerChatEvent(
                        filteredText,
                        player.getDisplayName(),
                        player.getUUID().toString()
                );
            }
        }
    }
}
