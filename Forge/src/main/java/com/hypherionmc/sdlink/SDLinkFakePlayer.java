package com.hypherionmc.sdlink;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.hypherionmc.sdlink.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Supplier;

public class SDLinkFakePlayer extends CommandSourceStack {

    private static final UUID uuid = UUID.fromString(SDLinkConstants.FAKE_UUID);
    private final MessageReceivedEvent event;

    public SDLinkFakePlayer(MinecraftServer server, int perm, String name, MessageReceivedEvent event) {
        super(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, server.overworld(), perm, "SDLinkFakePlayer", Component.literal(name), server, null);
        this.event = event;
    }

    @Override
    public void sendSuccess(Supplier<Component> component, boolean bl) {
        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages) {
            try {
                String msg = ModUtils.resolve(component.get());
                event.getMessage().reply(msg).mentionRepliedUser(false).queue();
            } catch (Exception e) {}
        }
    }

    @Override
    public void sendFailure(Component component) {
        sendSuccess(() -> component, false);
    }
}