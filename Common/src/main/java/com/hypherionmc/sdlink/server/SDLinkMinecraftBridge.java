package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.craterlib.utils.ChatUtils;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.MessageContext;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.SDLinkRelayConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.experimental.ExperimentalFeatures;
import com.hypherionmc.sdlink.core.relay.RelayMessage;
import com.hypherionmc.sdlink.core.relay.SDLinkRelayClient;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.util.translations.Text;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import shadow.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SDLinkMinecraftBridge {

    final Pattern patternStart = Pattern.compile("%(.*?)(?:\\|(.*?))?%", Pattern.CASE_INSENSITIVE);

    public void discordMessageReceived(MessageContext context) {
        try {
            Component component = context.getFormattedMessageComponent();
            if (component == null) return;

            if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayDiscordChats) {
                RelayMessage relayMessage = RelayMessage.of(
                        RelayMessage.MessageType.DISCORD,
                        SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                        null,
                        ChatUtils.getAdventureSerializer().serialize(component)
                );

                SDLinkRelayClient.INSTANCE.relayMessage(relayMessage);
            }

            ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                    component,
                    false
            );

        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                e.printStackTrace();
                SDLinkConstants.LOGGER.error("Failed to send message: {}", e.getMessage());
            }
        }
    }

    public Result checkWhitelisting() {
        boolean enabled = ServerEvents.getInstance().getMinecraftServer().isUsingWhitelist();
        return enabled ? Result.success("Server is using whitelisting") : Result.error("Server side whitelisting is disabled");
    }

    public Pair<Integer, Integer> getPlayerCounts() {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();

        int playerCount = server.getPlayers().stream().filter(SDLinkMCPlatform.INSTANCE::playerIsActive).toList().size();
        return Pair.of(playerCount, server.getMaxPlayers());
    }

    public List<MinecraftAccount> getOnlinePlayers() {
        List<MinecraftAccount> accounts = new ArrayList<>();
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();

        if (server != null) {
            server.getPlayers().stream().filter(SDLinkMCPlatform.INSTANCE::playerIsActive).forEach(p -> {
                MinecraftAccount account = MinecraftAccount.of(p.getGameProfile());
                accounts.add(account);
            });
        }

        return accounts;
    }

    public long getServerUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - ServerEvents.getInstance().getUptime());
    }

    public String getServerVersion() {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return Text.translate("error.unknown") + " - " + Text.translate("error.unknown") ;
        return server.getServerModName() + " - " + server.getName();
    }

    public void executeMinecraftCommand(String command, int permLevel, MessageReceivedEvent event, @Nullable SDLinkAccount account, CompletableFuture<Result> replier) {
        String name = event.getMember().getEffectiveName();
        if (account != null) {
            name = account.getInGameName();
        }

        command = command.replace("%linked_user%", name);
        command = command.replace("%role%", event.getMember().getRoles().stream().map(Role::getName).collect(Collectors.joining()));

        if (!SDLinkConfig.INSTANCE.chatConfig.useLinkedNames)
            name = SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName;

        SDLinkMCPlatform.INSTANCE.executeCommand(command, permLevel, name, replier);
    }

    public boolean isOnlineMode() {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return false;

        if (ModloaderEnvironment.INSTANCE.isModLoaded("fabrictailor"))
            return true;

        return server.usesAuthentication();
    }

    public void banPlayer(MinecraftAccount minecraftAccount) {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return;

        BridgedGameProfile profile = BridgedGameProfile.mojang(minecraftAccount.getUuid(), minecraftAccount.getUsername());
        server.banPlayer(profile);
    }
}
