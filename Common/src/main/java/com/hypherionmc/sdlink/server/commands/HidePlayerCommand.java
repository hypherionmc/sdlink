package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;

public final class HidePlayerCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand command = CraterCommand.literal("hideplayer")
                .requiresPermission(4)
                .withNode("sdlink.muteplayer")
                .withGameProfilesArgument("username", (player, profiles, ctx) -> {
                   if (profiles.isEmpty()) {
                       ctx.sendSuccess(() -> Text.literal(SDText.translate("error.hiding.no_account_supplied").toString()), true);
                       return 1;
                   }

                    CraterGameProfile profile = profiles.get(0);
                    Result result = HiddenPlayersManager.INSTANCE.hidePlayer(profile.getId().toString(), profile.getName(), "minecraft");
                    ctx.sendSuccess(() -> Text.literal(result.getMessage()), true);
                    return 1;
                });

        event.registerCommand(command);
    }

}
