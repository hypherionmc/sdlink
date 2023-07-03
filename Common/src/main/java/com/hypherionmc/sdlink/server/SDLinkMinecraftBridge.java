package com.hypherionmc.sdlink.server;

import com.google.common.collect.Lists;
import com.hypherionmc.craterlib.core.platform.CommonPlatform;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.core.services.helpers.IMinecraftHelper;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.Member;
import com.hypherionmc.sdlink.util.ModUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SDLinkMinecraftBridge implements IMinecraftHelper {

    @Override
    public void discordMessageReceived(Member member, String s1) {
        if (SDLinkConfig.INSTANCE.generalConfig.debugging) SDLinkConstants.LOGGER.info("Got message {} from {}", s1, member.getEffectiveName());

        try {
            MutableComponent component = ModUtils.resolve(
                    SDLinkConfig.INSTANCE.messageFormatting
                            .mcPrefix.replace("%user%", member.getEffectiveName()) + s1)
                    .copy();

            ServerEvents.getInstance().getMinecraftServer().getPlayerList().broadcastMessage(
                    component,
                    ChatType.SYSTEM,
                    Util.NIL_UUID
            );
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to send message: {}", e.getMessage());
            }
        }
    }

    @Override
    public Result checkWhitelisting() {
        boolean enabled = ServerEvents.getInstance().getMinecraftServer().getPlayerList().isUsingWhitelist();
        return enabled ? Result.success("Server is using whitelisting") : Result.error("Server side whitelisting is disabled");
    }

    @Override
    public Result isPlayerWhitelisted(MinecraftAccount minecraftAccount) {
        GameProfile profile = new GameProfile(minecraftAccount.getUuid(), minecraftAccount.getUsername());
        UserWhiteList whiteList = ServerEvents.getInstance().getMinecraftServer().getPlayerList().getWhiteList();
        return whiteList.isWhiteListed(profile) ? Result.success("Player is whitelisted") : Result.error("Player is not whitelisted");
    }

    @Override
    public Result whitelistPlayer(MinecraftAccount minecraftAccount) {
        GameProfile profile = new GameProfile(minecraftAccount.getUuid(), minecraftAccount.getUsername());
        UserWhiteList whiteList = ServerEvents.getInstance().getMinecraftServer().getPlayerList().getWhiteList();

        if (!whiteList.isWhiteListed(profile)) {
            whiteList.add(new UserWhiteListEntry(profile));
            ServerEvents.getInstance().getMinecraftServer().getPlayerList().reloadWhiteList();
            return Result.success("Player has been whitelisted");
        }

        return Result.error("Player is already whitelisted");
    }

    @Override
    public Result unWhitelistPlayer(MinecraftAccount minecraftAccount) {
        GameProfile profile = new GameProfile(minecraftAccount.getUuid(), minecraftAccount.getUsername());
        UserWhiteList whiteList = ServerEvents.getInstance().getMinecraftServer().getPlayerList().getWhiteList();

        if (whiteList.isWhiteListed(profile)) {
            whiteList.remove(new UserWhiteListEntry(profile));
            ServerEvents.getInstance().getMinecraftServer().getPlayerList().reloadWhiteList();
            kickNonWhitelisted();
            return Result.success("Player has been removed from the whitelist");
        }

        return Result.error("Player is not whitelisted");
    }

    @Override
    public List<MinecraftAccount> getWhitelistedPlayers() {
        List<MinecraftAccount> accounts = new ArrayList<>();
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();

        if (server != null && server.getPlayerList() != null) {
            for (String player : server.getPlayerList().getWhiteList().getUserList()) {
                MinecraftAccount account = MinecraftAccount.standard(player);
                accounts.add(account);
            }
        }

        return accounts;
    }

    @Override
    public Pair<Integer, Integer> getPlayerCounts() {
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        return Pair.of(server.getPlayerCount(), server.getMaxPlayers());
    }

    @Override
    public List<MinecraftAccount> getOnlinePlayers() {
        List<MinecraftAccount> accounts = new ArrayList<>();
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();

        if (server != null && server.getPlayerList() != null) {
            server.getPlayerList().getPlayers().forEach(p -> {
                MinecraftAccount account = MinecraftAccount.fromGameProfile(p.getGameProfile());
                accounts.add(account);
            });
        }

        return accounts;
    }

    @Override
    public long getServerUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - ServerEvents.getInstance().getUptime());
    }

    @Override
    public String getServerVersion() {
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return "Unknown - Unknown";
        return server.getServerModName() + " - " + server.getServerVersion();
    }

    @Override
    public Result executeMinecraftCommand(String s, String s1) {
        String command;
        if (!s1.isEmpty()) {
            command = s.replace("%args%", s1);
        } else {
            command = s.replace(" %args%", "").replace("%args%", "");
        }

        return SDLinkMCPlatform.INSTANCE.executeCommand(CommonPlatform.INSTANCE.getMCServer(), command);
    }

    private void kickNonWhitelisted() {
        PlayerList playerlist = ServerEvents.getInstance().getMinecraftServer().getPlayerList();
        UserWhiteList whitelist = playerlist.getWhiteList();

        for(ServerPlayer serverplayerentity : Lists.newArrayList(playerlist.getPlayers())) {
            if (!whitelist.isWhiteListed(serverplayerentity.getGameProfile())) {
                serverplayerentity.connection.disconnect(
                        new TranslatableComponent("multiplayer.disconnect.not_whitelisted")
                );
            }
        }
    }

    @Override
    public boolean isOnlineMode() {
        MinecraftServer server = CommonPlatform.INSTANCE.getMCServer();
        if (server == null)
            return false;

        if (ModloaderEnvironment.INSTANCE.isModLoaded("fabrictailor"))
            return true;

        return server.usesAuthentication();
    }
}
