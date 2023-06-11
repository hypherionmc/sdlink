package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class DiscordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> discordCommand =
                Commands.literal("discord")
                        .requires((commandSource) -> commandSource.hasPermission(0))
                        .executes(context -> {
                            if (SDLinkConfig.INSTANCE.botConfig.invite.inviteLink != null && !SDLinkConfig.INSTANCE.botConfig.invite.inviteLink.isEmpty()) {

                                MutableComponent message = Component.literal(
                                        SDLinkConfig.INSTANCE.botConfig.invite.inviteMessage
                                                .replace("%inviteurl%", SDLinkConfig.INSTANCE.botConfig.invite.inviteLink)
                                );

                                Style clickstyle = message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SDLinkConfig.INSTANCE.botConfig.invite.inviteLink));
                                message.withStyle(clickstyle);

                                context.getSource().sendSuccess(message, false);
                            }
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}