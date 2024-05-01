package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.core.managers.EmbedManager;
import shadow.kyori.adventure.text.Component;

public class ReloadEmbedsCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("reloadembeds")
                .requiresPermission(4)
                .executes(ctx -> {
                    EmbedManager.init();
                    ctx.sendSuccess(() -> Component.text("Reloaded Embeds"), false);
                });

        event.registerCommand(cmd);
    }

}
