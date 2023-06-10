package me.hypherionmc.sdlink.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.hypherionmc.sdlinklib.config.ModConfig;
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
                            if (ModConfig.INSTANCE.generalConfig.inviteCommandEnabled) {

                                MutableComponent message = Component.literal(ModConfig.INSTANCE.messageConfig
                                        .inviteMessage.replace("%inviteurl%", ModConfig.INSTANCE.generalConfig.inviteLink));

                                Style clickstyle = message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ModConfig.INSTANCE.generalConfig.inviteLink));
                                message.withStyle(clickstyle);

                                context.getSource().sendSuccess(() -> message, false);
                            }
                            return 0;
                        });

        dispatcher.register(discordCommand);
    }

}
