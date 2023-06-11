package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.sdlink.server.ServerEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author HypherionSA
 */
public class ReloadModCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("sdreload")
                        .requires((commandSource) -> commandSource.hasPermission(0))
                        .executes(context -> {
                            ServerEvents.reloadInstance(context.getSource().getServer());
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}