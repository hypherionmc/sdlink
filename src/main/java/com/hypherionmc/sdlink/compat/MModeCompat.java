package com.hypherionmc.sdlink.compat;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.mmode.api.events.MaintenanceModeEvent;
import com.hypherionmc.mmode.config.MaintenanceModeConfig;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import org.jetbrains.annotations.Nullable;

public class MModeCompat {

    public static boolean maintenanceActive = false;

    public static void init() {
        CraterEventBus.INSTANCE.registerEventListener(MModeCompat.class);
    }

    @CraterEventListener
    public static void maintenanceStart(MaintenanceModeEvent.MaintenanceStart event) {
        maintenanceActive = true;

        if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled && SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.sendMaintenanceStart) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.CUSTOM)
                    .author(DiscordAuthor.SERVER)
                    .message(SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.maintenanceStartMessage)
                    .build();

            message.sendMessage();
        }

        if (BotController.INSTANCE.isBotReady()) {
            BotController.INSTANCE.getJDA().getPresence().setStatus(SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.onlineStatus);
        }
    }

    @CraterEventListener
    public static void maintenanceEnd(MaintenanceModeEvent.MaintenanceEnd event) {
        maintenanceActive = false;

        if (SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.enabled && SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.sendMaintenanceEnd) {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.CUSTOM)
                    .author(DiscordAuthor.SERVER)
                    .message(SDLinkCompatConfig.INSTANCE.maintenanceModeCompat.maintenanceEndMessage)
                    .build();

            message.sendMessage();
        }

        if (BotController.INSTANCE.isBotReady()) {
            BotController.INSTANCE.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        }
    }

    @Nullable
    public static String getMotd() {
        if (MaintenanceModeConfig.INSTANCE != null) {
            return MaintenanceModeConfig.INSTANCE.getMotd();
        }

        return null;
    }
}
