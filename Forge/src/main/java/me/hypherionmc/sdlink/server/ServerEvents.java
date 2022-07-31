package me.hypherionmc.sdlink.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import me.hypherionmc.sdlink.SDLinkConstants;
import me.hypherionmc.sdlink.server.commands.DiscordCommand;
import me.hypherionmc.sdlink.server.commands.WhoisCommand;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.WhiteList;
import net.minecraft.server.management.WhitelistEntry;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class ServerEvents implements IMinecraftHelper {

    private final BotController botEngine;
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
        botEngine = new BotController(this, SDLinkConstants.LOG);
        botEngine.initializeBot();
    }

    // Modloader Events
    public void onCommandRegister(CommandDispatcher<CommandSource> dispatcher) {
        DiscordCommand.register(dispatcher);
        WhoisCommand.register(dispatcher);
    }

    public void onServerStarting(MinecraftServer server) {
        this.server = server;
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStarting) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.serverStarting,
                        "server",
                        "",
                        modConfig.messageDestinations.stopStartInChat
                );
            }
        }
    }

    public void onServerStarted() {
        if (botEngine != null && modConfig.general.enabled) {
            botEngine.checkWhitelisting();
            if (modConfig.chatConfig.serverStarted) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.serverStarted,
                        "server",
                        "",
                        modConfig.messageDestinations.stopStartInChat
                );
            }
        }
    }

    public void onServerStopping() {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopping) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.serverStopping,
                        "server",
                        "",
                        modConfig.messageDestinations.stopStartInChat
                );
            }
        }
    }

    public void onServerStoppedEvent() {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.serverStopped) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.serverStopped,
                        "server",
                        "",
                        modConfig.messageDestinations.stopStartInChat
                );
            }
            botEngine.shutdownBot();
        }
    }

    public void onServerChatEvent(String message, String user, UUID uuid) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.playerMessages) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.chat.replace("%player%", user).replace("%message%", message.replace("@everyone", "").replace("@Everyone", "").replace("@here", "").replace("@Here", "")),
                        user,
                        uuid.toString(),
                        true
                );
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

    public void playerJoinEvent(PlayerEntity player) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.playerJoined.replace("%player%", player.getDisplayName().getString()),
                        "server",
                        "",
                        modConfig.messageDestinations.joinLeaveInChat
                );
            }
        }
    }

    public void playerLeaveEvent(PlayerEntity player) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.joinAndLeaveMessages) {
                botEngine.sendToDiscord(
                        modConfig.messageConfig.playerLeft.replace("%player%", player.getDisplayName().getString()),
                        "server",
                        "",
                        modConfig.messageDestinations.joinLeaveInChat
                );
            }
        }
    }

    public void onPlayerDeath(PlayerEntity player, String message) {
        if (botEngine != null && modConfig.general.enabled) {
            if (modConfig.chatConfig.deathMessages) {
                botEngine.sendToDiscord(message, "server", "", modConfig.messageDestinations.deathInChat);
            }
        }
    }

    public void onPlayerAdvancement(String name, String advancement, String advancement_description) {
        if (botEngine != null && modConfig.chatConfig.advancementMessages) {
            botEngine.sendToDiscord(
                    modConfig.messageConfig.achievements.replace("%player%", name).replace("%title%", advancement).replace("%description%", advancement_description),
                    "server",
                    "",
                    modConfig.messageDestinations.advancementsInChat
            );
        }
    }

    // Mod Events

    @Override
    public void discordMessageEvent(String s, String s1) {
        server.getPlayerList().broadcastMessage(
                new StringTextComponent(modConfig.chatConfig.mcPrefix.replace("%user%", s) + s1),
                ChatType.SYSTEM,
                Util.NIL_UUID
        );
    }

    @Override
    public boolean isWhitelistingEnabled() {
        return server.getPlayerList().isUsingWhitelist();
    }

    @Override
    public boolean whitelistPlayer(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        WhiteList whiteList = server.getPlayerList().getWhiteList();

        if (!whiteList.isWhiteListed(profile)) {
            whiteList.add(new WhitelistEntry(profile));
            server.getPlayerList().reloadWhiteList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unWhitelistPlayer(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        WhiteList whiteList = server.getPlayerList().getWhiteList();

        if (whiteList.isWhiteListed(profile)) {
            whiteList.remove(new WhitelistEntry(profile));
            server.getPlayerList().reloadWhiteList();
            kickNonWhitelisted();
            return true;
        } else {
            return false;
        }
    }

    private void kickNonWhitelisted() {
        PlayerList playerlist = server.getPlayerList();
        WhiteList whitelist = playerlist.getWhiteList();

        for(ServerPlayerEntity serverplayerentity : Lists.newArrayList(playerlist.getPlayers())) {
            if (!whitelist.isWhiteListed(serverplayerentity.getGameProfile())) {
                serverplayerentity.connection.disconnect(
                        new TranslationTextComponent("multiplayer.disconnect.not_whitelisted")
                );
            }
        }
    }

    @Override
    public List<String> getWhitelistedPlayers() {
        return Arrays.stream(server.getPlayerList().getWhiteList().getUserList()).collect(Collectors.toList());
    }

    @Override
    public boolean isPlayerWhitelisted(String s, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, s);
        WhiteList whiteList = server.getPlayerList().getWhiteList();

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

    public BotController getBotEngine() {
        return botEngine;
    }

    public ModConfig getModConfig() {
        return modConfig;
    }
}
