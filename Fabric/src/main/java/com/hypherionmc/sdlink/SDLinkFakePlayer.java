package com.hypherionmc.sdlink;

import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.util.ModUtils;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SDLinkFakePlayer extends CommandSourceStack {

    private static final UUID uuid = UUID.fromString(SDLinkConstants.FAKE_UUID);

    public SDLinkFakePlayer(MinecraftServer server) {
        super(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, server.overworld(), 4, "SDLinkFakePlayer", new TextComponent("SDLinkFakePlayer"), server, null);
    }

    @Override
    public void sendSuccess(Component component, boolean bl) {
        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages) {
            try {
                String msg = ModUtils.resolve(component);
                DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CONSOLE)
                        .author(DiscordAuthor.SERVER)
                        .message(msg)
                        .build();

                discordMessage.sendMessage();
            } catch (Exception e) {}
        }
    }

    @Override
    public void sendFailure(Component component) {
        sendSuccess(component, false);
    }
}