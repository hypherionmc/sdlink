package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.nojang.commands.BridgedFakePlayer;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.craterlib.utils.ChatUtils;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.Result;
import shadow.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SDLinkFakePlayer extends BridgedFakePlayer {

    private final CompletableFuture<Result> replier;

    public SDLinkFakePlayer(BridgedMinecraftServer server, int perm, String name, CompletableFuture<Result> replier) {
        super(server, perm, name);
        this.replier = replier;
    }

    @Override
    public void onSuccess(Supplier<Component> supplier, Boolean aBoolean) {
        try {
            String msg = ChatUtils.resolve(supplier.get(), SDLinkConfig.INSTANCE.chatConfig.formatting);
            replier.complete(Result.success(msg));
        } catch (Exception e) {
            replier.complete(Result.error("Failed to execute command: " + e.getMessage()));
        }
    }
}
