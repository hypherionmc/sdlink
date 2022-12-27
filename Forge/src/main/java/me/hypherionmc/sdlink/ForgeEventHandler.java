package me.hypherionmc.sdlink;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.sdlink.server.ServerEvents;
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

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        ServerEvents.getInstance().onCommandRegister(event.getDispatcher());
    }

    @SubscribeEvent
    public void serverStartedEvent(ServerAboutToStartEvent event) {
        ServerEvents.getInstance().onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public void serverStartedEvent(ServerStartingEvent event) {
        ServerEvents.getInstance().onServerStarted();
    }

    @SubscribeEvent
    public void serverStoppingEvent(ServerStoppingEvent event) {
        ServerEvents.getInstance().onServerStopping();
    }

    @SubscribeEvent
    public void serverStoppedEvent(ServerStoppedEvent event) {
        ServerEvents.getInstance().onServerStoppedEvent();
    }

    @SubscribeEvent
    public void serverChatEvent(ServerChatEvent event) {
        ServerEvents.getInstance().onServerChatEvent(event.getMessage(), event.getPlayer().getDisplayName(), event.getPlayer().getUUID().toString());
    }

    @SubscribeEvent
    public void commandEvent(CommandEvent event) {
        String command = event.getParseResults().getReader().getString();
        UUID uuid = null;
        try {
            uuid = event.getParseResults().getContext().getLastChild().getSource().getPlayerOrException().getUUID();
        } catch (CommandSyntaxException ignored) {}
        ServerEvents.getInstance().commandEvent(
                command,
                event.getParseResults().getContext().getLastChild().getSource().getDisplayName(),
                uuid != null ? uuid.toString() : ""
        );
    }

    @SubscribeEvent
    public void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        ServerEvents.getInstance().playerJoinEvent(event.getEntity());
    }

    @SubscribeEvent
    public void playerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerEvents.getInstance().playerLeaveEvent(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            ServerEvents.getInstance().onPlayerDeath(
                    player,
                    event.getSource().getLocalizedDeathMessage(event.getEntity())
            );
        }
    }

    @SubscribeEvent
    public void onPlayerAdvancement(AdvancementEvent event) {
        if (event.getAdvancement() != null && event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat()) {
            ServerEvents.getInstance().onPlayerAdvancement(
                    event.getEntity().getDisplayName(),
                    event.getAdvancement().getDisplay().getTitle(),
                    event.getAdvancement().getDisplay().getDescription()
            );
        }
    }
}
