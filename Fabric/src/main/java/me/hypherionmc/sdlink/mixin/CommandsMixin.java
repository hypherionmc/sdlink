package me.hypherionmc.sdlink.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.sdlink.SDLinkFabric;
import me.hypherionmc.sdlink.SafeCalls;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(
            method = "performCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void injectPerformCommand(ParseResults<CommandSourceStack> parse, String string, CallbackInfoReturnable<Integer> cir, CommandSourceStack commandSourceStack) {
        try {
            if (FabricLoader.getInstance().isModLoaded("fabrictailor")) {
                SafeCalls.tailorPlayerJoin(parse.getContext().getLastChild().getSource().getPlayerOrException(), string);
            } else {
                ServerEvents.getInstance().commandEvent(
                        string,
                        parse.getContext().getLastChild().getSource().getDisplayName().getString(),
                        parse.getContext().getLastChild().getSource().getPlayerOrException().getUUID().toString()
                );
            }
        } catch (CommandSyntaxException e) {
            ServerEvents.getInstance().commandEvent(
                    string,
                    parse.getContext().getLastChild().getSource().getDisplayName().getString(),
                    null
            );
        }
    }

}
