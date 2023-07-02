package com.hypherionmc.sdlink.mixin;

import com.hypherionmc.sdlink.server.ServerEvents;
import me.vetustus.server.simplechat.api.event.PlayerChatCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author HypherionSA
 * SimpleChat's API is broken... So we mixin to make it work
 */
@Mixin(PlayerChatCallback.ChatMessage.class)
public class SimpleChatMixin {

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void injectEvent(ServerPlayer sender, String message, CallbackInfo ci) {
        if (sender == null)
            return;

        ServerEvents.getInstance().onServerChatEvent(
                new TextComponent(message.replaceFirst("!", "")),
                sender.getDisplayName(),
                sender.getStringUUID(),
                sender.getGameProfile(),
                false);
    }
}
