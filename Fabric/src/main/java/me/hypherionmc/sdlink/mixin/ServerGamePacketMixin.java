package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SafeCalls;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketMixin {

    @Shadow public ServerPlayer player;

    @Redirect(
            method = "handleChat(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
            )
    )
    public void onGameMessage(PlayerList instance, Component component, ChatType chatType, UUID uUID) {
        if (!component.getString().startsWith("/")) {
            if (FabricLoader.getInstance().isModLoaded("fabrictailor")) {
                SafeCalls.tailerPlayerMessage(player, component);
            } else {
                ServerEvents.getInstance().onServerChatEvent(
                        component,
                        player.getDisplayName(),
                        player.getUUID().toString()
                );
            }
        }
        instance.broadcastMessage(component, chatType, uUID);
    }

}
