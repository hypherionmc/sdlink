package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.events.ServerEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleDiscordLink extends JavaPlugin {

    private ServerEvents events;

    @Override
    public void onEnable() {
        events = new ServerEvents(this);
        getServer().getPluginManager().registerEvents(events, this);

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (events != null) events.onServerStarted();
        }, 30);
    }

    @Override
    public void onDisable() {
        if (events != null) {
            events.serverStoppedEvent();
        }
    }
}
