/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.util;

import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
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
public class LogReader extends AbstractAppender {

    public static String logs = "";
    private static boolean isDevEnv = false;
    private long time;
    private Thread messageScheduler;

    protected LogReader(String name, Filter filter) {
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
        LogReader da = LogReader.createAppender("SDLinkLogging", null);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(da);
        da.start();
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
                while (true) {
                    if (!BotController.INSTANCE.isBotReady())
                        return;
                    if (System.currentTimeMillis() - time > 250) {
                        logs = logs.replaceAll("\\b(?:(?:2(?:[0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9])\\.){3}(?:(?:2([0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9]))\\b", "[REDACTED]");

                        if (logs.length() > 2000) {
                            logs = logs.substring(0, 1999);
                        }

                        DiscordMessage discordMessage = new DiscordMessageBuilder(MessageType.CONSOLE)
                                .message(logs)
                                .author(DiscordAuthor.SERVER)
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
                            BotController.INSTANCE.getLogger().error("Failed to send console message: {}", e.getMessage());
                        }
                    }
                }
            });
            messageScheduler.start();
        }
    }
}
