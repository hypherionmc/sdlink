package me.hypherionmc.sdlink.events;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.sdlink.commands.DiscordCommand;
import me.hypherionmc.sdlink.commands.WhoisCommand;
import me.hypherionmc.sdlinklib.config.ConfigEngine;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.BotEngine;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerEvents implements MinecraftEventHandler {

    private final ModConfig modConfig;
    private final BotEngine botEngine;
    private MinecraftServer server;
    private long uptime = System.currentTimeMillis();

    public ServerEvents() {
        ConfigEngine configEngine = new ConfigEngine(System.getProperty("user.dir") + "/config");
        modConfig = configEngine.getModConfig();
        botEngine = new BotEngine(modConfig, this);

        if (botEngine != null && modConfig.general.enabled) {
            botEngine.initBot();
        }
    }

    // Forge Events
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        DiscordCommand.register(dispatcher);
        WhoisCommand.register(dispatcher);
    }

    @SubscribeEvent
    public void serverStartedEvent(ServerAboutToStartEvent event) {
        server = event.getServer();
        if (botEngine != null && modConfig.general.enabled) {
            botEngine.initWhitelisting();
            if (modConfig.chatConfig.serverStarting) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStarting, "", "", false);
            }
        }
    }

    @SubscribeEvent
    public void serverStartingEvent(ServerStartingEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStarted) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStarted, "", "", false);
            }
        }
    }

    @SubscribeEvent
    public void serverStoppingEvent(ServerStoppingEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopping) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStopping, "", "", false);
            }
        }
    }

    @SubscribeEvent
    public void serverStoppedEvent(ServerStoppedEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopped) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStopped, "", "", false);
            }
            botEngine.shutdownBot();
        }
    }

    @SubscribeEvent
    public void serverChatEvent(ServerChatEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.playerMessages) {
                botEngine.sendToDiscord(event.getMessage().replace("@everyone", "").replace("@Everyone", "").replace("@here", "").replace("@Here", ""), event.getUsername(), event.getPlayer().getUUID().toString(), true);
            }
        }
    }

    @SubscribeEvent
    public void commandEvent(CommandEvent event) {
        String command = event.getParseResults().getReader().getString().replaceFirst(Pattern.quote("/"), "");

        if (modConfig.chatConfig.broadcastCommands) {
            botEngine.sendToDiscord(event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString() + " **executed command: " + command + "**", event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString(), "", false);
        }

        if ((command.startsWith("say") || command.startsWith("me")) && botEngine != null && modConfig.chatConfig.sendSayCommand) {
            String msg = command.startsWith("say") ? command.substring(4) : command.substring(3);
            try {
                botEngine.sendToDiscord(msg, event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString(), event.getParseResults().getContext().getLastChild().getSource().getPlayerOrException().getUUID().toString(), true);
            } catch (CommandSyntaxException e) {
                botEngine.sendToDiscord(msg, event.getParseResults().getContext().getLastChild().getSource().getDisplayName().getString(), "", true);
            }
        }
    }

    @SubscribeEvent
    public void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord(modConfig.messageConfig.playerJoined.replace("%player%", event.getPlayer().getDisplayName().getString()), "", "", false);
            }
        }
    }

    @SubscribeEvent
    public void playerJoinEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord( modConfig.messageConfig.playerLeft.replace("%player%", event.getPlayer().getDisplayName().getString()), "", "", false);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof Player) {
            if (botEngine != null && modConfig.general.enabled) {
                if (modConfig.chatConfig.deathMessages) {
                    botEngine.sendToDiscord(event.getSource().getLocalizedDeathMessage(event.getEntityLiving()).getString(), "", "", false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerAdvancement(AdvancementEvent event) {
        if (botEngine != null && event.getAdvancement() != null && event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat() && modConfig.chatConfig.advancementMessages) {
            botEngine.sendToDiscord(event.getPlayer().getDisplayName().getString() + " has made the advancement [" + ChatFormatting.stripFormatting(event.getAdvancement().getDisplay().getTitle().getString()) + "]: " + ChatFormatting.stripFormatting(event.getAdvancement().getDisplay().getDescription().getString()), "", "", false);
        }
    }

    // Mod Events
    @Override
    public void discordMessageReceived(String s, String s1) {
        if (server.getPlayerList() != null && !server.getPlayerList().getPlayers().isEmpty()) {
            server.getPlayerList().getPlayers().forEach(player -> {
                player.displayClientMessage(new TextComponent(ChatFormatting.YELLOW + "[Discord] " + ChatFormatting.RESET + s + ": " + s1), false);
            });
        }
    }

    @Override
    public boolean whiteListingEnabled() {
        return server.getPlayerList().isUsingWhitelist();
    }

    @Override
    public String whitelistPlayer(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        if (!whiteList.isWhiteListed(profile)) {
            whiteList.add(new UserWhiteListEntry(profile));
            server.getPlayerList().reloadWhiteList();
            return s + " is now whitelisted";
        } else {
            return s + " is already whitelisted";
        }
    }

    @Override
    public String unWhitelistPlayer(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        if (whiteList.isWhiteListed(profile)) {
            whiteList.remove(new UserWhiteListEntry(profile));
            server.getPlayerList().reloadWhiteList();
            kickNonWhitelisted();
            return s + " has been removed from the whitelist";
        } else {
            return s + " is not whitelisted";
        }
    }

    private void kickNonWhitelisted() {
        PlayerList playerlist = server.getPlayerList();
        UserWhiteList whitelist = playerlist.getWhiteList();

        for(ServerPlayer serverplayerentity : Lists.newArrayList(playerlist.getPlayers())) {
            if (!whitelist.isWhiteListed(serverplayerentity.getGameProfile())) {
                serverplayerentity.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
            }
        }
    }

    @Override
    public List<String> getWhitelistedPlayers() {
        return Arrays.stream(server.getPlayerList().getPlayerNamesArray()).collect(Collectors.toList());
    }

    @Override
    public int getPlayerCount() {
        return server.getPlayerCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayers();
    }

    @Override
    public List<String> getOnlinePlayers() {
        if (server != null && server.getPlayerList() != null) {
            return Arrays.asList(server.getPlayerNames());
        }
        return new ArrayList<>();
    }

    @Override
    public long getServerUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - uptime);
    }

    @Override
    public float getTPS() {
        float meanTickTime = (float) (SystemUtils.meanTime(server.tickTimes) * 1.0E-6D);
        return (float) Math.min(1000.0 / meanTickTime, 20);
    }

    @Override
    public String getServerVersion() {
        return server.getServerModName() + " - " + server.getServerVersion();
    }

    @Override
    public void sendStopCommand() {
        server.halt(false);
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

    public BotEngine getBotEngine() {
        return botEngine;
    }

    public void onServerCrashed() {
        if (botEngine != null && modConfig.general.enabled) {
            botEngine.sendToDiscord("Server has crashed!...", "", "", false);
        }
    }
}
