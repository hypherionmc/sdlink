package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.core.managers.EmbedManager;

public final class ReloadEmbedsCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("reloadembeds")
                .requiresPermission(4)
                .withNode("sdlink.reloadembeds")
                .execute(ctx -> {
                    EmbedManager.init();
                    ctx.sendSuccess(() -> Text.literal("Reloaded Embeds"), false);
                    return 1;
                });

        event.registerCommand(cmd);
    }

}
