package me.hypherionmc.sdlink;

import me.hypherionmc.mcdiscordformatter.discord.DiscordSerializer;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SDLinkFakePlayer extends CommandSourceStack {

    private static final UUID uuid = UUID.fromString(SDLinkConstants.FAKE_UUID);

    public SDLinkFakePlayer(MinecraftServer server) {
        super(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, server.overworld(), 4, "SDLinkFakePlayer", Component.literal("SDLinkFakePlayer"), server, null);
    }

    @Override
    public void sendSuccess(Component component, boolean bl) {
        String msg = ChatFormatting.stripFormatting(component.getString());
        try {
            msg = DiscordSerializer.INSTANCE.serialize(component.copy());
        } catch (Exception e) {}
        ServerEvents.getInstance().getBotEngine().sendConsoleMessage("server", msg);
    }

    @Override
    public void sendFailure(Component component) {
        sendSuccess(component, false);
    }
}
