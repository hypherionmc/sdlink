package me.hypherionmc.sdlink.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlink.SimpleDiscordLink;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("discord")
                        .requires((commandSource) -> commandSource.hasPermission(0))
                        .executes(context -> {
                            if (SimpleDiscordLink.serverEvents != null && SimpleDiscordLink.serverEvents.getModConfig().chatConfig.inviteCommandEnabled) {
                                Style style = Style.EMPTY;
                                style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SimpleDiscordLink.serverEvents.getModConfig().general.inviteLink));
                                context.getSource().sendSuccess(
                                        new TextComponent(
                                                SimpleDiscordLink.serverEvents
                                                        .getModConfig().messageConfig
                                                        .inviteMessage.replace("%inviteurl%", SimpleDiscordLink.serverEvents.getModConfig().general.inviteLink)
                                        ).setStyle(style)
                                        , true);
                            }
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}
