package me.hypherionmc.sdlink.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.SimpleDiscordLink;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class WhoisCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("whois")
                        .requires((commandSource) -> commandSource.hasPermission(2))
                        .then(Commands.argument("username", StringArgumentType.string()).executes(context -> {
                            if (SimpleDiscordLink.serverEvents != null && SimpleDiscordLink.serverEvents.getBotEngine() != null) {
                                String username = StringArgumentType.getString(context, "username");
                                String value = SimpleDiscordLink.serverEvents.getBotEngine().getDiscordName(username);
                                context.getSource().sendSuccess(new TextComponent(value), true);
                            }
                            return 1;
                        }));
        dispatcher.register(discordCommand);
    }

}
