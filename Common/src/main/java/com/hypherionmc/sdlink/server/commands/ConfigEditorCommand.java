package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlinkrw.modules.editor.ConfigEditorServer;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;

public final class ConfigEditorCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("sdconfigeditor")
                .requiresPermission(4)
                .withNode("sdlink.configeditor")
                .execute(ctx -> {
                    ConfigEditorServer.INSTANCE.createServer(ctx);
                    ctx.sendSuccess(() -> Text.literal(SDText.translate("mc.sdconfigeditor.opening").toString()), false);
                    return 1;
                });

        event.registerCommand(cmd);
    }

}
