package me.hypherionmc.sdlink;

import com.mojang.authlib.GameProfile;
import me.hypherionmc.mcdiscordformatter.discord.DiscordSerializer;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class SDLinkFakePlayer extends FakePlayer {

    private static final UUID uuid = UUID.fromString(SDLinkConstants.FAKE_UUID);

    public SDLinkFakePlayer(MinecraftServer server) {
        super(server.overworld(), new GameProfile(uuid, "SDLinkFakePlayer"));
    }

    @Override
    public void sendMessage(ITextComponent component, UUID uuid) {
        String msg = TextFormatting.stripFormatting(component.getString());
        try {
            msg = DiscordSerializer.INSTANCE.serialize(component.copy());
        } catch (Exception e) {}
        ServerEvents.getInstance().getBotEngine().sendConsoleMessage("server", msg);
    }

    @Override
    public void displayClientMessage(ITextComponent chatComponent, boolean actionBar) {
        sendMessage(chatComponent, Util.NIL_UUID);
    }

    @Override
    protected int getPermissionLevel() {
        return 4;
    }
}
