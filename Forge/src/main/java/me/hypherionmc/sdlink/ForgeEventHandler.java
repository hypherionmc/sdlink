package me.hypherionmc.sdlink;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.sdlink.server.ServerEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
        MutableComponent chatComponent = new TextComponent(event.getMessage());
        chatComponent.withStyle(event.getComponent().getStyle());
        ServerEvents.getInstance().onServerChatEvent(chatComponent, event.getPlayer().getName(), event.getPlayer().getUUID().toString());
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
                    new TextComponent(event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString()),
                    uuid != null ? uuid.toString() : ""
            );
    }

    @SubscribeEvent
    public void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        ServerEvents.getInstance().playerJoinEvent(event.getPlayer());
    }

    @SubscribeEvent
    public void playerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerEvents.getInstance().playerLeaveEvent(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            ServerEvents.getInstance().onPlayerDeath(
                    player,
                    event.getSource().getLocalizedDeathMessage(event.getEntityLiving())
            );
        }
    }

    @SubscribeEvent
    public void onPlayerAdvancement(AdvancementEvent event) {
        if (event.getAdvancement() != null && event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat()) {
            ServerEvents.getInstance().onPlayerAdvancement(
                    event.getPlayer().getName(),
                    event.getAdvancement().getDisplay().getTitle(),
                    event.getAdvancement().getDisplay().getDescription()
            );
        }
    }

}
