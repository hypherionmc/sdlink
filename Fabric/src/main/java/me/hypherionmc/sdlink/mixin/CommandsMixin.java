package me.hypherionmc.sdlink.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.sdlink.SDLinkFabric;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)I", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private void injectPerformCommand(CommandSourceStack commandSourceStack, String string, CallbackInfoReturnable<Integer> cir, StringReader stringReader) {
        try {
            ParseResults<CommandSourceStack> parse = dispatcher.parse(stringReader, commandSourceStack);
            try {
                SDLinkFabric.serverEvents.commandEvent(stringReader.getString(), parse.getContext().getLastChild().getSource().getDisplayName().getString(), parse.getContext().getLastChild().getSource().getPlayerOrException().getUUID());
            } catch (CommandSyntaxException e) {
                SDLinkFabric.serverEvents.commandEvent(stringReader.getString(), parse.getContext().getLastChild().getSource().getDisplayName().getString(), null);
            }
        } catch (Exception e) {}
    }

}
