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
import java.util.function.Function;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketMixin {

    @Shadow public ServerPlayer player;

    @Redirect(
            method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
            )
    )
    public void onGameMessage(PlayerList instance, Component filteredText, Function<ServerPlayer, Component> function, ChatType chatType, UUID uUID) {
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
        instance.broadcastMessage(filteredText, function, chatType, uUID);
    }
}
