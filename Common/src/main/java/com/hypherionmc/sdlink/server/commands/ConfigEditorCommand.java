package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.core.editor.ConfigEditorClient;
import com.hypherionmc.sdlink.util.translations.Text;
import shadow.kyori.adventure.text.Component;

public class ConfigEditorCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("sdconfigeditor")
                .requiresPermission(4)
                .withNode("sdlink.configeditor")
                .execute(ctx -> {
                    ConfigEditorClient.INSTANCE.openConnection();
                    ctx.sendSuccess(() -> Component.text(Text.translate("mc.sdconfigeditor.opening").toString()), false);
                    return 1;
                });

        event.registerCommand(cmd);
    }

}
