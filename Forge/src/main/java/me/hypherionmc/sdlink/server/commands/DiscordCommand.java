package me.hypherionmc.sdlink.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> discordCommand =
                Commands.literal("discord")
                        .requires((commandSource) -> commandSource.hasPermission(0))
                        .executes(context -> {
                            if (ServerEvents.getInstance().getModConfig().generalConfig.inviteCommandEnabled) {

                                IFormattableTextComponent message = new StringTextComponent(ServerEvents.getInstance()
                                        .getModConfig().messageConfig
                                        .inviteMessage.replace("%inviteurl%", ServerEvents.getInstance().getModConfig().generalConfig.inviteLink));

                                Style clickstyle = message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ServerEvents.getInstance().getModConfig().generalConfig.inviteLink));
                                message.withStyle(clickstyle);

                                context.getSource().sendSuccess(message, false);
                            }
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}
