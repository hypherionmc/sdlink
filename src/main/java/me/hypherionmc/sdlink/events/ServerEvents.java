package me.hypherionmc.sdlink.events;

import me.hypherionmc.sdlink.SimpleDiscordLink;
import me.hypherionmc.sdlinklib.config.ConfigEngine;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.BotEngine;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getServer;

public class ServerEvents implements MinecraftEventHandler, Listener {

    private final ModConfig modConfig;
    private final BotEngine botEngine;
    private Server server;
    private final SimpleDiscordLink plugin;
    private long uptime = System.currentTimeMillis();

    public ServerEvents(SimpleDiscordLink plugin) {
        this.plugin = plugin;
        File confPath = new File(System.getProperty("user.dir") + "/plugins/SDLink");
        if (!confPath.exists()) {
            confPath.mkdirs();
        }
        ConfigEngine configEngine = new ConfigEngine(confPath.getAbsolutePath());
        modConfig = configEngine.getModConfig();
        botEngine = new BotEngine(modConfig, this);

        if (botEngine != null && modConfig.general.enabled) {
            botEngine.initBot();

            try {
                for (int i = 0; i <= 5; i++) {
                    if (!botEngine.isBotReady()) Thread.sleep(1000);
                    else break;
                }
            } catch (Exception ignored) {}

            if (botEngine.isBotReady()) {
                if (modConfig.chatConfig.serverStarting) {
                    botEngine.sendToDiscord(modConfig.messageConfig.serverStarting, "", "", false);
                }
            }
        }
        server = getServer();
    }

    public void onServerStarted() {
        if (botEngine != null && modConfig.general.enabled) {
            botEngine.initWhitelisting();
            if (modConfig.chatConfig.serverStarted) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStarted, "", "", false);
            }
        }
    }

    @EventHandler
    public void serverStoppingEvent(PluginDisableEvent event) {
        if (botEngine != null && modConfig.general.enabled && event.getPlugin() == plugin) {
            if (modConfig.chatConfig.serverStopping) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStopping, "", "", false);
                this.serverStoppedEvent();
            }
        }
    }

    public void serverStoppedEvent() {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopped) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStopped, "", "", false);
            }
            botEngine.shutdownBot();
        }
    }

    @EventHandler
    public void serverChatEvent(AsyncPlayerChatEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.playerMessages) {
                botEngine.sendToDiscord(event.getMessage().replace("@everyone", "").replace("@Everyone", "").replace("@here", "").replace("@Here", ""), event.getPlayer().getDisplayName(), event.getPlayer().getUniqueId().toString(), true);
            }
        }
    }

    @EventHandler
    public void onCommandEvent(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();

        if (modConfig.chatConfig.broadcastCommands) {
            botEngine.sendToDiscord(event.getPlayer().getDisplayName() + " **executed command: " + command + "**", event.getPlayer().getDisplayName(), "", false);
        }

        if ((command.startsWith("say") || command.startsWith("me")) && botEngine != null && modConfig.chatConfig.sendSayCommand) {
            String msg = command.startsWith("say") ? command.substring(4) : command.substring(3);
            botEngine.sendToDiscord(msg, event.getPlayer().getDisplayName(), "", true);
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerLoginEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord(modConfig.messageConfig.playerJoined.replace("%player%", event.getPlayer().getDisplayName()), "", "", false);
            }
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord( modConfig.messageConfig.playerLeft.replace("%player%", event.getPlayer().getDisplayName()), "", "", false);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.deathMessages) {
                botEngine.sendToDiscord(event.getDeathMessage(), "", "", false);
            }
        }
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        // TODO Find a way to implement advancements. Spigot does not appear to have a way to get the actual title/description
    }

    //Mod Events
    @Override
    public void discordMessageReceived(String s, String s1) {
        if (server.getOnlinePlayers() != null && !server.getOnlinePlayers().isEmpty()) {
            server.getOnlinePlayers().forEach(player -> {
                player.sendMessage(ChatColor.YELLOW + "[Discord] " + ChatColor.RESET + s + ": " + s1);
            });
        }
    }

    @Override
    public boolean whiteListingEnabled() {
        return server.hasWhitelist();
    }

    @Override
    public String whitelistPlayer(String s, UUID uuid) {
        OfflinePlayer player = server.getOfflinePlayer(s);

        if (!server.getWhitelistedPlayers().contains(player)) {
            player.setWhitelisted(true);
            server.reloadWhitelist();
            return s + " is now whitelisted";
        } else {
            return s + " is already whitelisted";
        }
    }

    @Override
    public String unWhitelistPlayer(String s, UUID uuid) {
        OfflinePlayer player = server.getOfflinePlayer(s);

        if (server.getWhitelistedPlayers().contains(player)) {
            player.setWhitelisted(false);
            server.reloadWhitelist();
            kickNonWhitelisted();
            return s + " has been removed from the whitelist";
        } else {
            return s + " is not whitelisted";
        }
    }

    private void kickNonWhitelisted() {
        server.getScheduler().runTask(plugin, () -> {
        Collection<? extends Player> players = server.getOnlinePlayers();

        for(Player serverplayerentity : players) {
            if (!server.getWhitelistedPlayers().contains(serverplayerentity)) {
                serverplayerentity.kickPlayer("You are not whitelisted on this server");
            }
        }
        });
    }

    @Override
    public List<String> getWhitelistedPlayers() {
        List<String> playerNames = new ArrayList<>();
        server.getWhitelistedPlayers().forEach(offlinePlayer -> {
            playerNames.add(offlinePlayer.getName());
        });
        return playerNames;
    }

    @Override
    public int getPlayerCount() {
        return server.getOnlinePlayers().size();
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayers();
    }

    @Override
    public List<String> getOnlinePlayers() {
        List<String> playerNames = new ArrayList<>();
        server.getOnlinePlayers().forEach(onlinePlayers -> {
            playerNames.add(onlinePlayers.getName());
        });
        return playerNames;
    }

    @Override
    public long getServerUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - uptime);
    }

    @Override
    public float getTPS() {
        // TODO Implement
        return (float) 0f;
    }

    @Override
    public String getServerVersion() {
        return server.getName() + " - " + server.getVersion();
    }

    @Override
    public void sendStopCommand() {
        server.shutdown();
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

    public BotEngine getBotEngine() {
        return botEngine;
    }
}
