package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile;
import com.hypherionmc.craterlib.api.game.server.CraterGameServer;
import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.craterlib.api.loader.CraterLoader;
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
import com.hypherionmc.sdlink.util.Debugger;
import com.hypherionmc.sdlink.util.translations.SDText;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

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
            Text component = context.getFormattedMessageComponent();
            if (component == null) return;

            if (ExperimentalFeatures.INSTANCE.RELAY_SERVER && SDLinkRelayConfig.INSTANCE.messageConfig.relayDiscordChats) {
                RelayMessage relayMessage = RelayMessage.of(
                        RelayMessage.MessageType.DISCORD,
                        SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                        null,
                        component.toJsonString()
                );

                SDLinkRelayClient.INSTANCE.relayMessage(relayMessage);
            }

            ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                    component,
                    false
            );

        } catch (Exception e) {
            Debugger.INSTANCE.log("Failed to send message: {}", e.getMessage());
        }
    }

    public Result checkWhitelisting() {
        boolean enabled = ServerEvents.getInstance().getMinecraftServer().isUsingWhitelist();
        return enabled ? Result.success("Server is using whitelisting") : Result.error("Server side whitelisting is disabled");
    }

    public Pair<Integer, Integer> getPlayerCounts() {
        CraterGameServer server = ServerEvents.getInstance().getMinecraftServer();

        int playerCount = server.getPlayers().stream().filter(SDLinkMCPlatform.INSTANCE::playerIsActive).toList().size();
        return Pair.of(playerCount, server.getMaxPlayers());
    }

    public List<MinecraftAccount> getOnlinePlayers() {
        List<MinecraftAccount> accounts = new ArrayList<>();
        CraterGameServer server = ServerEvents.getInstance().getMinecraftServer();

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
        CraterGameServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return SDText.translate("error.unknown") + " - " + SDText.translate("error.unknown") ;
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
        CraterGameServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return false;

        if (CraterLoader.isModLoaded("fabrictailor"))
            return true;

        return server.usesAuthentication();
    }

    public void banPlayer(MinecraftAccount minecraftAccount) {
        CraterGameServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return;

        CraterGameProfile profile = CraterGameProfile.fromGame(minecraftAccount.getUsername(), minecraftAccount.getUuid());
        server.banPlayer(profile);
    }
}
