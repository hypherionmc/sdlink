package me.hypherionmc.sdlink;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeEventHandler {

    private static final ServerEvents serverEvents = ServerEvents.getInstance();

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        serverEvents.onCommandRegister(event.getDispatcher());
    }

    @SubscribeEvent
    public void serverStartedEvent(ServerAboutToStartEvent event) {
        serverEvents.onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public void serverStartedEvent(ServerStartingEvent event) {
        serverEvents.onServerStarted();
    }

    @SubscribeEvent
    public void serverStoppingEvent(ServerStoppingEvent event) {
        serverEvents.onServerStopping();
    }

    @SubscribeEvent
    public void serverStoppedEvent(ServerStoppedEvent event) {
        serverEvents.onServerStoppedEvent();
    }

    @SubscribeEvent
    public void serverChatEvent(ServerChatEvent event) {
        serverEvents.onServerChatEvent(event.getMessage(), event.getUsername(), event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public void commandEvent(CommandEvent event) {
        String command = event.getParseResults().getReader().getString();
        UUID uuid = null;
        try {
            uuid = event.getParseResults().getContext().getLastChild().getSource().getPlayerOrException().getUUID();
        } catch (CommandSyntaxException ignored) {}
        serverEvents.commandEvent(command, event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString(), uuid);
    }

    @SubscribeEvent
    public void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        serverEvents.playerJoinEvent(event.getPlayer());
    }

    @SubscribeEvent
    public void playerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        serverEvents.playerLeaveEvent(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            serverEvents.onPlayerDeath(player, event.getSource().getLocalizedDeathMessage(event.getEntityLiving()).getString());
        }
    }

    @SubscribeEvent
    public void onPlayerAdvancement(AdvancementEvent event) {
        if (event.getAdvancement() != null && event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat()) {
            serverEvents.onPlayerAdvancement(event.getPlayer().getDisplayName().getString(), ChatFormatting.stripFormatting(event.getAdvancement().getDisplay().getTitle().getString()), ChatFormatting.stripFormatting(event.getAdvancement().getDisplay().getDescription().getString()));
        }
    }

}
