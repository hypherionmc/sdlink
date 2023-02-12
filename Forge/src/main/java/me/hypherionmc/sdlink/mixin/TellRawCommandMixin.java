package me.hypherionmc.sdlink.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.impl.TellRawCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;


@Mixin(TellRawCommand.class)
public class TellRawCommandMixin {

    private static AtomicReference<Boolean> hasSent = new AtomicReference<>(false);

    @SuppressWarnings("unchecked")
    @Inject(method = "register", at = @At(value = "HEAD"), cancellable = true)
    private static void injectTellRaw(CommandDispatcher<CommandSource> commandDispatcher, CallbackInfo ci) {
        if (ServerEvents.getInstance().getModConfig() != null && ServerEvents.getInstance().getModConfig().messageConfig.relayTellRaw) {
            ci.cancel();

            commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) Commands.literal("tellraw").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", ComponentArgument.textComponent()).executes((commandContext) -> {
                int i = 0;

                for(Iterator<ServerPlayerEntity> var2 = EntityArgument.getPlayers(commandContext, "targets").iterator(); var2.hasNext(); ++i) {
                    ServerPlayerEntity serverPlayer = var2.next();

                    if (!hasSent.get()) {
                        if (commandContext.getSource().getEntity() instanceof ServerPlayerEntity) {
                            ServerPlayerEntity player = (ServerPlayerEntity) commandContext.getSource().getEntity();

                            ServerEvents.getInstance().onServerChatEvent(
                                    ComponentArgument.getComponent(commandContext, "message"),
                                    player.getDisplayName(),
                                    player.getUUID().toString()
                            );
                        } else {
                            ServerEvents.getInstance().onServerChatEvent(
                                    ComponentArgument.getComponent(commandContext, "message"),
                                    new StringTextComponent("Server"),
                                    "",
                                    true
                            );
                        }
                        hasSent.set(true);
                    }

                    serverPlayer.sendMessage(TextComponentUtils.updateForEntity(commandContext.getSource(), ComponentArgument.getComponent(commandContext, "message"), serverPlayer, 0), Util.NIL_UUID);
                }

                hasSent.set(false);
                return i;
            }))));
        }
    }

}
