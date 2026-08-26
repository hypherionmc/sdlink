/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.craterlib.api.loader.CraterLoader;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.compat.MModeCompat;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.BotConfigSettings;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.server.SDLinkMinecraftBridge;
import com.hypherionmc.sdlink.util.SystemUtils;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author HypherionSA
 * Hooks to run when the bot is ready
 */
public final class BotReadyHooks {

    /**
     * Update the bot activity
     *
     * @param event The {@link ReadyEvent}
     */
    public static void startActivityUpdates(ReadyEvent event) {
        if (SDLinkConfig.INSTANCE.botConfig.statusUpdateInterval > 0) {
            BotController.INSTANCE.updatesManager.scheduleAtFixedRate(() -> {
                try {
                    if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
                        if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled
                                && CraterLoader.isModLoaded("mmode")
                                && MModeCompat.getMotd() != null
                                && !MModeCompat.getMotd().isEmpty()
                                && MModeCompat.maintenanceActive
                                && SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.updateBotStatus) {
                            event.getJDA().getPresence().setActivity(Activity.customStatus(MModeCompat.getMotd()));
                        } else {
                            BotConfigSettings.BotStatus newStatus = SDLinkConfig.INSTANCE.botConfig.botStatus.getNextRandom().orElse(null);

                            if (newStatus == null)
                                return;

                            Activity act = Activity.of(newStatus.botStatusType, newStatus.botStatus
                                    .replace("%players%", String.valueOf(SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getLeft()))
                                    .replace("%maxplayers%", String.valueOf(SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getRight())));

                            if (newStatus.botStatusType == Activity.ActivityType.STREAMING) {
                                act = Activity.of(newStatus.botStatusType, newStatus.botStatus
                                                .replace("%players%", String.valueOf(SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getLeft()))
                                                .replace("%maxplayers%", String.valueOf(SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getRight())),
                                        newStatus.botStatusStreamingURL);
                            }

                            event.getJDA().getPresence().setActivity(act);
                        }

                    }
                } catch (Exception e) {
                    if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                        SDLinkConstants.LOGGER.info(e.getMessage());
                    }
                }

                if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled && CraterLoader.isModLoaded("mmode")) {
                    event.getJDA().getPresence().setStatus(MModeCompat.maintenanceActive ? SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.onlineStatus : OnlineStatus.ONLINE);
                }
            }, SDLinkConfig.INSTANCE.botConfig.statusUpdateInterval, SDLinkConfig.INSTANCE.botConfig.statusUpdateInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Update the Chat Channel topic, if enabled
     */
    public static void startTopicUpdates() {
        if (!SDLinkConfig.INSTANCE.botConfig.channelTopic.doTopicUpdates)
            return;

        BotController.INSTANCE.updatesManager.scheduleAtFixedRate(() -> {
            try {
                if (BotController.INSTANCE.isBotReady() && (SDLinkConfig.INSTANCE.botConfig.channelTopic.channelTopic != null && !SDLinkConfig.INSTANCE.botConfig.channelTopic.channelTopic.isEmpty())) {
                    List<GuildMessageChannel> channels = SDLCache.INSTANCE.getChannelDestinations(MessageType.CHAT);

                    channels.forEach(channel -> {
                        if (channel instanceof StandardGuildMessageChannel mc) {
                            if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled
                                    && CraterLoader.isModLoaded("mmode")
                                    && MModeCompat.getMotd() != null
                                    && !MModeCompat.getMotd().isEmpty()
                                    && MModeCompat.maintenanceActive
                                    && SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.updateChannelTopic) {
                                mc.getManager().setTopic(MModeCompat.getMotd()).queue();
                            } else {
                                String topic = SDLinkConfig.INSTANCE.botConfig.channelTopic.channelTopic
                                        .replace("%players%", String.valueOf(SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getLeft()))
                                        .replace("%maxplayers%", String.valueOf(SDLinkMinecraftBridge.INSTANCE.getPlayerCounts().getRight()))
                                        .replace("%uptime%", SystemUtils.secondsToTimestamp(SDLinkMinecraftBridge.INSTANCE.getServerUptime()));
                                mc.getManager().setTopic(topic).queue();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                    SDLinkConstants.LOGGER.info(e.getMessage());
                }
            }
        }, Math.max(6, SDLinkConfig.INSTANCE.botConfig.channelTopic.updateInterval), Math.max(6, SDLinkConfig.INSTANCE.botConfig.channelTopic.updateInterval), TimeUnit.MINUTES);
    }
}
