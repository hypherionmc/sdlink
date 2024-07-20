package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.server.ServerEvents;

public class ReloadBotCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("reloadbot")
                .requiresPermission(4)
                .executes(ctx -> ServerEvents.reloadBot());

        event.registerCommand(cmd);
    }

}
