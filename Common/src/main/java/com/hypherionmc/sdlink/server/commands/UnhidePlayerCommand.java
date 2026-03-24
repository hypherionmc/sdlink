package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.managers.HiddenPlayersManager;
import com.hypherionmc.sdlink.util.translations.SDText;

public final class UnhidePlayerCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand command = CraterCommand.literal("unhideplayer")
                .requiresPermission(4)
                .withNode("sdlink.unmuteplayer")
                .withGameProfilesArgument("username", (player, profiles, ctx) -> {
                    if (profiles.isEmpty()) {
                        ctx.sendSuccess(() -> Text.literal(SDText.translate("error.hiding.unhide_no_account_provided").toString()), true);
                        return 1;
                    }

                    CraterGameProfile profile = profiles.get(0);
                    Result result = HiddenPlayersManager.INSTANCE.unhidePlayer(profile.getId().toString());
                    ctx.sendSuccess(() -> Text.literal(result.getMessage()), true);
                    return 1;
                });

        event.registerCommand(command);
    }

}
