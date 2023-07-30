package me.hypherionmc.sdlink.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.level.ServerPlayer;
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
    private static void injectTellRaw(CommandDispatcher<CommandSourceStack> commandDispatcher, CallbackInfo ci) {
        if (ServerEvents.getInstance().getModConfig() != null && ServerEvents.getInstance().getModConfig().messageConfig.relayTellRaw) {
            ci.cancel();

            commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) Commands.literal("tellraw").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", ComponentArgument.textComponent()).executes((commandContext) -> {
                int i = 0;

                for(Iterator<ServerPlayer> var2 = EntityArgument.getPlayers(commandContext, "targets").iterator(); var2.hasNext(); ++i) {
                    ServerPlayer serverPlayer = var2.next();

                    if (!hasSent.get()) {
                        if (commandContext.getSource().getEntity() instanceof ServerPlayer player) {
                            ServerEvents.getInstance().onServerChatEvent(
                                    ComponentArgument.getComponent(commandContext, "message"),
                                    player.getDisplayName(),
                                    player.getGameProfile(),
                                    player.getUUID().toString()
                            );
                        } else {
                            ServerEvents.getInstance().onServerChatEvent(
                                    ComponentArgument.getComponent(commandContext, "message"),
                                    Component.literal("Server"),
                                    "",
                                    null,
                                    true
                            );
                        }
                        hasSent.set(true);
                    }

                    serverPlayer.sendSystemMessage(ComponentUtils.updateForEntity(commandContext.getSource(), ComponentArgument.getComponent(commandContext, "message"), serverPlayer, 0), false);
                }

                hasSent.set(false);
                return i;
            }))));
        }
    }

}
