package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.server.ServerEvents;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;

public final class ReloadBotCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("reloadbot")
                .requiresPermission(4)
                .withNode("sdlink.reloadbot")
                .execute(ctx -> {
                    ServerEvents.reloadBot(true);
                    ctx.sendSuccess(() -> Text.literal(SDText.translate("feedback.bot_reloaded").toString()), true);
                    return 1;
                });

        event.registerCommand(cmd);
    }

}
