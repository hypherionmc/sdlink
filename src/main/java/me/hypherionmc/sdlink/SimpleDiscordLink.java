package me.hypherionmc.sdlink;

import me.hypherionmc.sdlink.commands.DiscordCommand;
import me.hypherionmc.sdlink.events.ServerEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleDiscordLink extends JavaPlugin {

    public static ServerEvents events;

    @Override
    public void onEnable() {
        events = new ServerEvents(this);
        getServer().getPluginManager().registerEvents(events, this);

        this.getCommand("discord").setExecutor(new DiscordCommand());
        this.getCommand("whois").setExecutor(new DiscordCommand());

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (events != null) events.onServerStarted();
        }, 30);
    }

    @Override
    public void onDisable() {
    }
}
