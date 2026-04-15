package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.util.SDLinkUtils;
import com.hypherionmc.sdlink.util.translations.SDText;
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;

public final class DiscordVerifyCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("discordverify")
                .requiresPermission(0)
                .withNode("sdlink.discord_verify")
                .execute(ctx -> {
                    if (!ctx.isPlayer() || ctx.getPlayer() == null) {
                        ctx.sendFailure(Text.literal(SDText.translate("error.verify.only_by_players").toString()));
                        return 1;
                    }

                    if (!SDLinkConfig.INSTANCE.accessControl.enabled && !SDLinkConfig.INSTANCE.accessControl.optionalVerification) {
                        ctx.sendFailure(Text.literal(SDText.translate("error.verify.not_enabled").toString()));
                        return 1;
                    }

                    MinecraftAccount account = MinecraftAccount.of(ctx.getPlayer().getGameProfile());
                    SDLinkAccount sdLinkAccount = account.getStoredAccount();

                    if (sdLinkAccount == null) {
                        ctx.sendFailure(Text.literal(SDText.translate("account.load_failed").toString()));
                        return 1;
                    }

                    if (SDLinkUtils.isNullOrEmpty(sdLinkAccount.getVerifyCode())) {
                        int code = SDLinkUtils.intInRange(1000, 9999);
                        sdLinkAccount.setVerifyCode(String.valueOf(code));
                        DatabaseManager.INSTANCE.updateEntry(sdLinkAccount);
                        ctx.sendSuccess(() -> Text.literal(SDLinkConfig.INSTANCE.accessControl.verificationMessages.optionalVerificationMessage.replace("{code}", String.valueOf(code))), false);
                    } else {
                        ctx.sendSuccess(() -> Text.literal(SDLinkConfig.INSTANCE.accessControl.verificationMessages.optionalVerificationMessage.replace("{code}", String.valueOf(sdLinkAccount.getVerifyCode()))), false);
                    }
                    return 1;
                });

        event.registerCommand(cmd);
    }

}
