package me.hypherionmc.sdlink.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("discord")
                        .requires((commandSource) -> commandSource.hasPermission(0))
                        .executes(context -> {
                            if (ServerEvents.getInstance().getModConfig().chatConfig.inviteCommandEnabled) {
                                Style style = Style.EMPTY;
                                style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ServerEvents.getInstance().getModConfig().general.inviteLink));
                                context.getSource().sendSuccess(
                                        Component.literal(
                                                ServerEvents.getInstance()
                                                        .getModConfig().messageConfig
                                                        .inviteMessage.replace("%inviteurl%", ServerEvents.getInstance().getModConfig().general.inviteLink)
                                        ).setStyle(style)
                                        , true);
                            }
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}
