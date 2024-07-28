package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import shadow.kyori.adventure.text.Component;

public class UnhidePlayerCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand command = CraterCommand.literal("unhideplayer")
                .requiresPermission(4)
                .withGameProfileArgument("username", (player, profiles, ctx) -> {
                    if (profiles.isEmpty()) {
                        ctx.sendSuccess(() -> Component.text("You need to supply a player to unhide"), true);
                        return;
                    }

                    BridgedGameProfile profile = profiles.get(0);
                    HiddenPlayersManager.INSTANCE.unhidePlayer(profile.getId().toString());
                    ctx.sendSuccess(() -> Component.text("Player " + profile.getName() + " is now visible"), true);
                });

        event.registerCommand(command);
    }

}
