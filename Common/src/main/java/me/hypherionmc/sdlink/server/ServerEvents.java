package me.hypherionmc.sdlink.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import me.hypherionmc.sdlink.server.commands.DiscordCommand;
import me.hypherionmc.sdlink.server.commands.WhoisCommand;
import me.hypherionmc.sdlinklib.config.ConfigController;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.BotEngine;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServerEvents implements IMinecraftHelper {

    private final ModConfig modConfig;
    private final BotEngine botEngine;
    private MinecraftServer server;
    private final long uptime = System.currentTimeMillis();

    private static ServerEvents events;

    public static ServerEvents getInstance() {
        if (events == null) {
            events = new ServerEvents();
        }
        return events;
    }

    private ServerEvents() {
        ConfigController configController = new ConfigController(System.getProperty("user.dir") + File.separator + "config");
        modConfig = configController.getModConfig();
        botEngine = new BotEngine(this, modConfig);

        if (modConfig.general.enabled) {
            botEngine.initBot();
        }
    }

    // Modloader Events
    public void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
        DiscordCommand.register(dispatcher);
        WhoisCommand.register(dispatcher);
    }

    public void onServerStarting(MinecraftServer server) {
        this.server = server;
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStarting) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStarting, "server", "", modConfig.messageDestinations.stopStartInChat);
            }
        }
    }

    public void onServerStarted() {
        if (botEngine != null && modConfig.general.enabled) {
            botEngine.initWhitelisting();
            if (modConfig.chatConfig.serverStarted) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStarted, "server", "", modConfig.messageDestinations.stopStartInChat);
            }
        }
    }

    public void onServerStopping() {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopping) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStopping, "server", "", modConfig.messageDestinations.stopStartInChat);
            }
        }
    }

    public void onServerStoppedEvent() {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopped) {
                botEngine.sendToDiscord(modConfig.messageConfig.serverStopped, "server", "", modConfig.messageDestinations.stopStartInChat);
            }
            botEngine.shutdownBot();
        }
    }

    public void onServerChatEvent(String message, String user, UUID uuid) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.playerMessages) {
                botEngine.sendToDiscord(message.replace("@everyone", "").replace("@Everyone", "").replace("@here", "").replace("@Here", ""), user, uuid.toString(), true);
            }
        }
    }

    public void commandEvent(String cmd, String name, UUID uuid) {
        String command = cmd;
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }

        if (!command.startsWith("say") && !command.startsWith("me")) {
            command = command.split(" ")[0];
        }

        if (modConfig.chatConfig.broadcastCommands) {
            botEngine.sendToDiscord(name + " **executed command: " + command + "**", name, "", false);
        }

        if ((command.startsWith("say") || command.startsWith("me")) && botEngine != null && modConfig.chatConfig.sendSayCommand) {
            String msg = command.startsWith("say") ? command.replace("say ", "").replace("say", "") : command.replace("me ", "").replace("me", "");
            botEngine.sendToDiscord(msg, name, uuid == null ? "" : uuid.toString(), true);
        }
    }

    public void playerJoinEvent(Player player) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord(modConfig.messageConfig.playerJoined.replace("%player%", player.getDisplayName().getString()), "server", "", modConfig.messageDestinations.joinLeaveInChat);
            }
        }
    }

    public void playerLeaveEvent(Player player) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord(modConfig.messageConfig.playerLeft.replace("%player%", player.getDisplayName().getString()), "server", "", modConfig.messageDestinations.joinLeaveInChat);
            }
        }
    }

    public void onPlayerDeath(Player player, String message) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.deathMessages) {
                botEngine.sendToDiscord(message, "server", "", modConfig.messageDestinations.deathInChat);
            }
        }
    }

    public void onPlayerAdvancement(String name, String advancement, String advancement_description) {
        if (botEngine != null && modConfig.chatConfig.advancementMessages) {
            botEngine.sendToDiscord(name + " has made the advancement [" + advancement + "]: " + advancement_description, "", "", modConfig.messageDestinations.advancementsInChat);
        }
    }

    // Mod Events

    @Override
    public void discordMessageEvent(String s, String s1) {
        server.getPlayerList().broadcastMessage(new TextComponent(ChatFormatting.YELLOW + "[Discord] " + ChatFormatting.RESET + s + ": " + s1), ChatType.CHAT, Util.NIL_UUID);
    }

    @Override
    public boolean isWhitelistingEnabled() {
        return server.getPlayerList().isUsingWhitelist();
    }

    @Override
    public boolean whitelistPlayer(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        if (!whiteList.isWhiteListed(profile)) {
            whiteList.add(new UserWhiteListEntry(profile));
            server.getPlayerList().reloadWhiteList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unWhitelistPlayer(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        if (whiteList.isWhiteListed(profile)) {
            whiteList.remove(new UserWhiteListEntry(profile));
            server.getPlayerList().reloadWhiteList();
            kickNonWhitelisted();
            return true;
        } else {
            return false;
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
    public boolean isPlayerWhitelisted(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        UserWhiteList whiteList = server.getPlayerList().getWhiteList();

        return whiteList.isWhiteListed(profile);
    }

    @Override
    public int getOnlinePlayerCount() {
        return server.getPlayerCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayers();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
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
    public String getServerVersion() {
        return server.getServerModName() + " - " + server.getServerVersion();
    }

    // Other

    public BotEngine getBotEngine() {
        return botEngine;
    }

    public ModConfig getModConfig() {
        return modConfig;
    }
}
