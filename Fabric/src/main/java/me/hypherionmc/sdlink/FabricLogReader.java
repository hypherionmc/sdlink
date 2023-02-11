package me.hypherionmc.sdlink;

import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.discord.DiscordMessage;
import me.hypherionmc.sdlinklib.discord.messages.MessageAuthor;
import me.hypherionmc.sdlinklib.discord.messages.MessageType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

@Plugin(name = "SDLinkLogging", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class FabricLogReader extends AbstractAppender {

    private static BotController botEngine;

    public static String logs = "";
    private long time;
    private Thread messageScheduler;
    private static boolean isDevEnv = false;

    protected FabricLogReader(String name, Filter filter) {
        super(name, filter, null, true);
    }

    @PluginFactory
    public static FabricLogReader createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new FabricLogReader(name, filter);
    }

    public static void init(BotController botController, boolean isDev) {
        botEngine = botController;
        isDevEnv = isDev;
        FabricLogReader da = FabricLogReader.createAppender("SDLinkLogging", null);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(da);
        da.start();
    }

    @Override
    public void append(LogEvent event) {
        if (botEngine.isBotReady()) {
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
                    if (!botEngine.isBotReady())
                        return;
                    if (System.currentTimeMillis() - time > 250) {
                        if (logs.length() > 2000) {
                            logs = logs.substring(0, 1999);
                        }

                        DiscordMessage message = new DiscordMessage.Builder(botEngine, MessageType.CONSOLE).withMessage(logs).withAuthor(MessageAuthor.SERVER).build();

                        if (modConfig.messageConfig.sendConsoleMessages) {
                            message.sendMessage();
                        }

                        logs = "";
                        break;
                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        if (modConfig.generalConfig.debugging) {
                            BotController.LOGGER.error("Failed to send console message: {}", e.getMessage());
                        }
                    }
                }
            });
            messageScheduler.start();
        }
    }
}
