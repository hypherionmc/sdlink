package com.hypherionmc.sdlink.server;

import com.hypherionmc.craterlib.core.platform.CommonPlatform;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.core.services.helpers.IMinecraftHelper;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.Member;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.entities.Role;
import com.hypherionmc.sdlink.shaded.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserBanListEntry;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

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
        MutableComponent component = Component.empty();
        Style baseStyle = Style.EMPTY;
        Matcher matcher = patternStart.matcher(prefix);

        int lastAppendPosition = 0;

        while (matcher.find()) {
            String var = matcher.group(1);

            component.append(Component.literal(prefix.substring(lastAppendPosition, matcher.start())).withStyle(baseStyle));
            lastAppendPosition = matcher.end();

            if (var != null) {
                switch (var) {
                    case "color" -> baseStyle = baseStyle.withColor(TextColor.fromRgb(member.getColorRaw()));
                    case "end_color" -> baseStyle = baseStyle.withColor(ChatFormatting.WHITE);
                }
            }
        }

        component.append(Component.literal(prefix.substring(lastAppendPosition)).withStyle(baseStyle));

        try {
            MutableComponent finalComponent = component.append(SDLinkChatUtils.parseChatLinks(s1));

            ServerEvents.getInstance().getMinecraftServer().getPlayerList().broadcastSystemMessage(
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
        boolean enabled = ServerEvents.getInstance().getMinecraftServer().getPlayerList().isUsingWhitelist();
        return enabled ? Result.success("Server is using whitelisting") : Result.error("Server side whitelisting is disabled");
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
        MinecraftServer server = ServerEvents.getInstance().getMinecraftServer();
        if (server == null)
            return "Unknown - Unknown";
        return server.getServerModName() + " - " + server.getServerVersion();
    }

    @Override
    public Result executeMinecraftCommand(String command, int permLevel, MessageReceivedEvent event, @Nullable SDLinkAccount account) {
        String name = event.getMember().getEffectiveName();
        if (account != null) {
            name = account.getUsername();
        }

        command = command.replace("%linked_user%", name);
        command = command.replace("%role%", event.getMember().getRoles().stream().map(Role::getName).collect(Collectors.joining()));

        return SDLinkMCPlatform.INSTANCE.executeCommand(command, permLevel, event, name);
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

    @Override
    public void banPlayer(MinecraftAccount minecraftAccount) {
        MinecraftServer server = CommonPlatform.INSTANCE.getMCServer();
        if (server == null)
            return;

        GameProfile profile = new GameProfile(minecraftAccount.getUuid(), minecraftAccount.getUsername());
        server.getPlayerList().getBans().add(new UserBanListEntry(profile));
    }
}
