/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.compat.MModeCompat;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.BotConfigSettings;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.ChannelManager;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import com.hypherionmc.sdlink.util.SystemUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;

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
                                && ModloaderEnvironment.INSTANCE.isModLoaded("mmode")
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
                                    .replace("%players%", String.valueOf(SDLinkPlatform.minecraftHelper.getPlayerCounts().getLeft()))
                                    .replace("%maxplayers%", String.valueOf(SDLinkPlatform.minecraftHelper.getPlayerCounts().getRight())));

                            if (newStatus.botStatusType == Activity.ActivityType.STREAMING) {
                                act = Activity.of(newStatus.botStatusType, newStatus.botStatus
                                                .replace("%players%", String.valueOf(SDLinkPlatform.minecraftHelper.getPlayerCounts().getLeft()))
                                                .replace("%maxplayers%", String.valueOf(SDLinkPlatform.minecraftHelper.getPlayerCounts().getRight())),
                                        newStatus.botStatusStreamingURL);
                            }

                            event.getJDA().getPresence().setActivity(act);
                        }

                    }
                } catch (Exception e) {
                    if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                        BotController.INSTANCE.getLogger().info(e.getMessage());
                    }
                }

                if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled && ModloaderEnvironment.INSTANCE.isModLoaded("mmode")) {
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
                    MessageChannel channel = ChannelManager.getDestinationChannel(MessageDestination.CHAT);
                    if (channel instanceof StandardGuildMessageChannel mc) {
                        if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled
                                && ModloaderEnvironment.INSTANCE.isModLoaded("mmode")
                                && MModeCompat.getMotd() != null
                                && !MModeCompat.getMotd().isEmpty()
                                && MModeCompat.maintenanceActive
                                && SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.updateChannelTopic) {
                            mc.getManager().setTopic(MModeCompat.getMotd()).queue();
                        } else {
                            String topic = SDLinkConfig.INSTANCE.botConfig.channelTopic.channelTopic
                                    .replace("%players%", String.valueOf(SDLinkPlatform.minecraftHelper.getPlayerCounts().getLeft()))
                                    .replace("%maxplayers%", String.valueOf(SDLinkPlatform.minecraftHelper.getPlayerCounts().getRight()))
                                    .replace("%uptime%", SystemUtils.secondsToTimestamp(SDLinkPlatform.minecraftHelper.getServerUptime()));
                            mc.getManager().setTopic(topic).queue();
                        }
                    }
                }
            } catch (Exception e) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                    BotController.INSTANCE.getLogger().info(e.getMessage());
                }
            }
        }, Math.max(6, SDLinkConfig.INSTANCE.botConfig.channelTopic.updateInterval), Math.max(6, SDLinkConfig.INSTANCE.botConfig.channelTopic.updateInterval), TimeUnit.MINUTES);
    }
}
