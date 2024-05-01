package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import shadow.kyori.adventure.text.Component;

public class WhoisCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("whois")
                .requiresPermission(4)
                .withGameProfileArgument("username", (player, profiles, ctx) -> {
                    if (BotController.INSTANCE != null) {
                        if (profiles.isEmpty()) {
                            ctx.sendSuccess(() -> Component.text("Unlinked"), true);
                            return;
                        }

                        MinecraftAccount account = MinecraftAccount.of(profiles.stream().findFirst().get());
                        String value = account.isAccountVerified() ? account.getDiscordName() : "Unlinked";
                        ctx.sendSuccess(() -> Component.text(value), true);
                    }
                });

        event.registerCommand(cmd);
    }

}