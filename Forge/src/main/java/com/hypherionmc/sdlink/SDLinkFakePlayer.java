package com.hypherionmc.sdlink;

import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import me.hypherionmc.mcdiscordformatter.discord.DiscordSerializer;
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
            String msg = ChatFormatting.stripFormatting(component.get().getString());
            try {
                msg = DiscordSerializer.INSTANCE.serialize(component.get().copy());
            } catch (Exception e) {}

            event.getMessage().reply(msg).mentionRepliedUser(false).queue();
        }
    }

    @Override
    public void sendFailure(Component component) {
        sendSuccess(() -> component, false);
    }
}