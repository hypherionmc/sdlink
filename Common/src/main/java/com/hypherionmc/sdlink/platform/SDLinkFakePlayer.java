package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.api.game.commands.CraterFakePlayer;
import com.hypherionmc.craterlib.api.game.server.CraterGameServer;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class SDLinkFakePlayer implements CraterFakePlayer {

    private final CompletableFuture<Result> replier;

    private SDLinkFakePlayer(CompletableFuture<Result> replier) {
        this.replier = replier;
    }

    public static CraterFakePlayer create(CraterGameServer server, int perm, String name, CompletableFuture<Result> replier) {
        SDLinkFakePlayer player = new SDLinkFakePlayer(replier);
        return CraterFakePlayer.create(server, perm, name, player);
    }

    // Not Used
    @Override
    public Object unwrapInternal() {
        return null;
    }

    @Override
    public void onSuccess(Supplier<Text> supplier, boolean aBoolean) {
        try {
            String msg = supplier.get().asString(SDLinkConfig.INSTANCE.chatConfig.formatting);
            replier.complete(Result.success(msg));
        } catch (Exception e) {
            replier.complete(Result.error(SDText.translate("error.mc_command_failed", e.getMessage())));
        }
    }
}
