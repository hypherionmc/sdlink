package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.sdlink.core.managers.EmbedManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadEmbedsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("reloadembeds")
                        .requires((commandSource) -> commandSource.hasPermission(4))
                        .executes(context -> {
                            EmbedManager.init();
                            context.getSource().sendSuccess(() -> Component.literal("Reloaded Embeds"), false);
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}
