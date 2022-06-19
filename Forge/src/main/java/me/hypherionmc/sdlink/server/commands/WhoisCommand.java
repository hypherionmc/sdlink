package me.hypherionmc.sdlink.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class WhoisCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> discordCommand =
                Commands.literal("whois")
                        .requires((commandSource) -> commandSource.hasPermission(2))
                        .then(Commands.argument("username", StringArgumentType.string()).executes(context -> {
                            if (ServerEvents.getInstance().getBotEngine() != null) {
                                String username = StringArgumentType.getString(context, "username");
                                String value = ServerEvents.getInstance().getBotEngine().getDiscordName(username);
                                context.getSource().sendSuccess(new StringTextComponent(value), true);
                            }
                            return 1;
                        }));
        dispatcher.register(discordCommand);
    }

}
