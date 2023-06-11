package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WhoisCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("whois")
                        .requires((commandSource) -> commandSource.hasPermission(2))
                        .then(Commands.argument("username", StringArgumentType.string()).executes(context -> {
                            if (BotController.INSTANCE != null) {
                                String username = StringArgumentType.getString(context, "username");
                                MinecraftAccount account = MinecraftAccount.standard(username);
                                String value;

                                if (account.isAccountLinked()) {
                                    value = account.getDiscordName();
                                } else {
                                    value = "Unlinked";
                                }
                                context.getSource().sendSuccess(() -> Component.literal(value), true);
                            }
                            return 1;
                        }));
        dispatcher.register(discordCommand);
    }

}