package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.core.services.helpers.IMinecraftHelper;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import shadow.kyori.adventure.text.Component;
import shadow.kyori.adventure.text.format.NamedTextColor;
import shadow.kyori.adventure.text.format.Style;
import shadow.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hypherionmc.sdlink.core.managers.DatabaseManager.sdlinkDatabase;

public class SDLinkMinecraftBridge implements IMinecraftHelper {

    final Pattern patternStart = Pattern.compile("%(.*?)(?:\\|(.*?))?%", Pattern.CASE_INSENSITIVE);

    @Override
    public void discordMessageReceived(Member member, String s1) {
        if (SDLinkConfig.INSTANCE.generalConfig.debugging) SDLinkConstants.LOGGER.info("Got message {} from {}", s1, member.getEffectiveName());

        AtomicReference<String> user = new AtomicReference<>(member.getEffectiveName());

        try {
            if (sdlinkDatabase != null) {
                List<SDLinkAccount> accounts = sdlinkDatabase.getCollection(SDLinkAccount.class);
                accounts.stream().filter(a -> a.getDiscordID().equals(member.getId())).findFirst().ifPresent(u -> {
                    user.set(u.getUsername());
                });
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to load account database: {}", e.getMessage());
            }
        }

        String prefix = SDLinkConfig.INSTANCE.messageFormatting.mcPrefix.replace("%user%", user.get());
        Component component = Component.empty();
        Style baseStyle = Style.empty();
        Matcher matcher = patternStart.matcher(prefix);

        int lastAppendPosition = 0;

        while (matcher.find()) {
            String var = matcher.group(1);

            component = component.append(Component.text(prefix.substring(lastAppendPosition, matcher.start())).style(baseStyle));
            lastAppendPosition = matcher.end();

            if (var != null) {
                switch (var) {
                    case "color" -> baseStyle = baseStyle.color(TextColor.color(member.getColorRaw()));
                    case "end_color" -> baseStyle = baseStyle.color(NamedTextColor.WHITE);
                }
            }
        }

        component = component.append(Component.text(prefix.substring(lastAppendPosition)).style(baseStyle));

        try {
            Component finalComponent = component.append(SDLinkChatUtils.parseChatLinks(s1));

            ServerEvents.getInstance().getMinecraftServer().broadcastSystemMessage(
                    finalComponent,
                    false
            );

        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to send message: {}", e.getMessage());
            }
        }
    }

    @Override
    public Result checkWhitelisting() {
        boolean enabled = ServerEvents.getInstance().getMinecraftServer().isUsingWhitelist();
        return enabled ? Result.success("Server is using whitelisting") : Result.error("Server side whitelisting is disabled");
    }

    @Override
    public Pair<Integer, Integer> getPlayerCounts() {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        return Pair.of(server.getPlayerCount(), server.getMaxPlayers());
    }

    @Override
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

    @Override
    public long getServerUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - ServerEvents.getInstance().getUptime());
    }

    @Override
    public String getServerVersion() {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return "Unknown - Unknown";
        return server.getServerModName() + " - " + server.getName();
    }

    @Override
    public Result executeMinecraftCommand(String command, int permLevel, MessageReceivedEvent event, @Nullable SDLinkAccount account) {
        String name = event.getMember().getEffectiveName();
        if (account != null) {
            name = account.getUsername();
        }

        command = command.replace("%linked_user%", name);
        command = command.replace("%role%", event.getMember().getRoles().stream().map(Role::getName).collect(Collectors.joining()));

        if (!SDLinkConfig.INSTANCE.chatConfig.useLinkedNames)
            name = SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName;

        return SDLinkMCPlatform.INSTANCE.executeCommand(command, permLevel, event, name);
    }

    @Override
    public boolean isOnlineMode() {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return false;

        if (ModloaderEnvironment.INSTANCE.isModLoaded("fabrictailor"))
            return true;

        return server.usesAuthentication();
    }

    @Override
    public void banPlayer(MinecraftAccount minecraftAccount) {
        BridgedMinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return;

        BridgedGameProfile profile = BridgedGameProfile.mojang(minecraftAccount.getUuid(), minecraftAccount.getUsername());
        server.banPlayer(profile);
    }
}
