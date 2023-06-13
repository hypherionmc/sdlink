package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class WhoisCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("whois")
                        .requires((commandSource) -> commandSource.hasPermission(2))
                        .then(Commands.argument("username", GameProfileArgument.gameProfile()).executes(context -> {
                            if (BotController.INSTANCE != null) {
                                Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "username");

                                if (profiles.isEmpty()) {
                                    context.getSource().sendSuccess(Component.literal("Unlinked"), true);
                                    return 1;
                                }

                                MinecraftAccount account = MinecraftAccount.fromGameProfile(profiles.stream().findFirst().get());
                                String value;

                                if (account.isAccountLinked()) {
                                    value = account.getDiscordName();
                                } else {
                                    value = "Unlinked";
                                }
                                context.getSource().sendSuccess(Component.literal(value), true);
                            }
                            return 1;
                        }));
        dispatcher.register(discordCommand);
    }

}