/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.api.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.api.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author HypherionSA
 * Log Appender to allow messages to be relayed from the Game Console to Discord
 */
@Plugin(name = "SDLinkLogging", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public final class LogReader extends AbstractAppender {

    public static String logs = "";
    private static boolean isDevEnv = false;
    private long time;
    private Thread messageScheduler;
    private static LogReader da;

    private LogReader(String name, Filter filter) {
        super(name, filter, null, true, new Property[0]);
    }

    @PluginFactory
    public static LogReader createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new LogReader(name, filter);
    }

    public static void init(boolean isDev) {
        isDevEnv = isDev;
        da = LogReader.createAppender("SDLinkLogging", null);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(da);
        da.start();
    }

    public static void destroy() {
        da.stop();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).removeAppender(da);
        da.messageScheduler.stop();
    }

    @Override
    public void append(LogEvent event) {
        if (BotController.INSTANCE.isBotReady()) {
            if (event.getLevel().intLevel() < Level.DEBUG.intLevel()) {
                logs += formatMessage(event) + "\n";
                scheduleMessage();
            }
        }
    }

    private String formatMessage(LogEvent event) {
        String devString = "**[" + formatTime(event.getTimeMillis()) + "]** " +
                "**[" + event.getThreadName() + "/" + event.getLevel().name() + "]** " +
                "**(" + event.getLoggerName().substring(event.getLoggerName().lastIndexOf(".") + 1) + ")** *" +
                event.getMessage().getFormattedMessage() + "*";

        String prodString = "**[" + formatTime(event.getTimeMillis()) + "]** " +
                "**[" + event.getThreadName() + "/" + event.getLevel().name() + "]** *" +
                event.getMessage().getFormattedMessage() + "*";

        return isDevEnv ? devString : prodString;
    }

    private String formatTime(long millis) {
        DateFormat obj = new SimpleDateFormat("HH:mm:ss");
        Date res = new Date(millis);
        return obj.format(res);
    }

    private void scheduleMessage() {
        time = System.currentTimeMillis();
        if (messageScheduler == null || !messageScheduler.isAlive()) {
            messageScheduler = new Thread(() -> {
                while (BotController.INSTANCE.isBotReady()) {
                    if (System.currentTimeMillis() - time > 250) {
                        logs = logs.replaceAll("\\b(?:(?:2(?:[0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9])\\.){3}(?:(?:2([0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9]))\\b", "[REDACTED]");
                        logs = logs.replaceAll("https:\\/\\/editor\\.firstdark\\.dev\\/[a-zA-Z0-9]+", "[REDACTED]");

                        if (logs.length() > 2000) {
                            logs = logs.substring(0, 1999);
                        }

                        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CONSOLE)
                                .message(logs)
                                .author(DiscordAuthor.getServer())
                                .build();

                        if (SDLinkConfig.INSTANCE.chatConfig.sendConsoleMessages) {
                            discordMessage.sendMessage();
                        }

                        logs = "";
                        break;
                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                            SDLinkConstants.LOGGER.error("Failed to send console message: {}", e.getMessage());
                        }
                    }
                }
            });
            messageScheduler.start();
        }
    }
}
