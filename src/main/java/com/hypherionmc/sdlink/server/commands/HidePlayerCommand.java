package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import shadow.kyori.adventure.text.Component;

public class HidePlayerCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand command = CraterCommand.literal("hideplayer")
                .requiresPermission(4)
                .withGameProfileArgument("username", (player, profiles, ctx) -> {
                   if (profiles.isEmpty()) {
                       ctx.sendSuccess(() -> Component.text("You need to supply a player to hide"), true);
                       return;
                   }

                    BridgedGameProfile profile = profiles.get(0);
                    HiddenPlayersManager.INSTANCE.hidePlayer(profile.getId().toString(), profile.getName(), "minecraft");
                    ctx.sendSuccess(() -> Component.text("Player " + profile.getName() + " is now hidden"), true);
                });

        event.registerCommand(command);
    }

}
