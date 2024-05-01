package com.hypherionmc.sdlink.platform;

import com.hypherionmc.craterlib.nojang.commands.BridgedFakePlayer;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.craterlib.utils.ChatUtils;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import shadow.kyori.adventure.text.Component;

import java.util.function.Supplier;

public class SDLinkFakePlayer extends BridgedFakePlayer {

    private final MessageReceivedEvent event;

    public SDLinkFakePlayer(BridgedMinecraftServer server, int perm, String name, MessageReceivedEvent event) {
        super(server, perm, name);
        this.event = event;
    }

    @Override
    public void onSuccess(Supplier<Component> supplier, Boolean aBoolean) {
        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages) {
            try {
                String msg = ChatUtils.resolve(supplier.get(), SDLinkConfig.INSTANCE.chatConfig.formatting);
                event.getMessage().reply(msg).mentionRepliedUser(false).queue();
            } catch (Exception e) {}
        }
    }
}
