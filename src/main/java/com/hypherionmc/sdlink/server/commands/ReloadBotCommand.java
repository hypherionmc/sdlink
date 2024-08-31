package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.server.ServerEvents;

public class ReloadBotCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("reloadbot")
                .requiresPermission(4)
                .withNode("sdlink.reloadbot")
                .execute(ctx -> {
                    ServerEvents.reloadBot();
                    return 1;
                });

        event.registerCommand(cmd);
    }

}
