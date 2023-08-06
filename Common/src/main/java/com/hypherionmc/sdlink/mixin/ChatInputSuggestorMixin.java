package com.hypherionmc.sdlink.mixin;

import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.shaded.javassist.bytecode.Opcode;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author HypherionSA
 * Allow Users, Roles and Channels to be pingable from MC chat (Client Side)
 */
@Mixin(CommandSuggestions.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow public abstract void showSuggestions(boolean p_93931_);

    @Shadow @Final
    EditBox input;

    @Shadow
    private static int getLastWordIndex(String p_93913_) {
        return 0;
    }

    @Inject(
            method = "updateCommandInfo",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/components/CommandSuggestions;pendingSuggestions:Ljava/util/concurrent/CompletableFuture;",
                    opcode = Opcode.PUTFIELD,
                    shift = At.Shift.AFTER,
                    ordinal = 0
            ),
            slice = @Slice(
                from = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/components/CommandSuggestions;getLastWordIndex(Ljava/lang/String;)I"
                )
            )
    )
    private void injectSuggestions(CallbackInfo ci) {
        this.showSuggestions(true);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "updateCommandInfo", at = @At(value = "STORE"), ordinal = 0, name = "collection")
    private Collection<String> injectMentions(Collection<String> vanilla) {
        ArrayList<String> newSuggest = new ArrayList<>(vanilla);

        if ((!ClientEvents.users.isEmpty() && ClientEvents.roles.isEmpty() && ClientEvents.channels.isEmpty())) {
            String currentInput = this.input.getValue();
            int currentCursorPosition = this.input.getCursorPosition();

            String textBeforeCursor = currentInput.substring(0, currentCursorPosition);
            int startOfCurrentWord = getLastWordIndex(textBeforeCursor);

            String currentWord = textBeforeCursor.substring(startOfCurrentWord);
            String finalWord = currentWord.replace("[", "").replace("]", "");

            ClientEvents.roles.keySet().stream().filter(p -> p.contains(finalWord)).forEach(k -> newSuggest.add("[" + k + "]"));
            ClientEvents.channels.keySet().stream().filter(p -> p.contains(finalWord)).forEach(k -> newSuggest.add("[" + k + "]"));
            ClientEvents.users.keySet().stream().filter(p -> p.contains(finalWord)).forEach(k -> newSuggest.add("[" + k + "]"));
        }

        return newSuggest;
    }
}
