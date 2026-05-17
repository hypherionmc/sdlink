package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;

public final class WhoisCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("whois")
                .requiresPermission(4)
                .withNode("sdlink.whois")
                .withGameProfilesArgument("username", (player, profiles, ctx) -> {
                    if (BotController.INSTANCE != null) {
                        if (profiles.isEmpty()) {
                            ctx.sendSuccess(() -> Text.literal(SDText.translate("account.unlinked").toString()), true);
                            return 1;
                        }

                        MinecraftAccount account = MinecraftAccount.of(profiles.stream().findFirst().get());
                        String value = account.isAccountVerified() ? account.getDiscordName() : SDText.translate("account.unlinked").toString();
                        ctx.sendSuccess(() -> Text.literal(value), true);
                    }
                    return 1;
                });

        event.registerCommand(cmd);
    }

}