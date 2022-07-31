package me.hypherionmc.sdlink.mixin;

import me.hypherionmc.sdlink.SDLinkFabric;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public void grantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat()) {
            SDLinkFabric.serverEvents.onPlayerAdvancement(
                    player.getDisplayName().getString(),
                    ChatFormatting.stripFormatting(advancement.getDisplay().getTitle().getString()),
                    ChatFormatting.stripFormatting(advancement.getDisplay().getDescription().getString())
            );
        }
    }

}
