/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.CommandManager;
import com.hypherionmc.sdlink.core.discord.events.DiscordEventHandler;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.managers.EmbedManager;
import com.hypherionmc.sdlink.core.managers.WebhookManager;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import com.hypherionmc.sdlink.util.ThreadedEventManager;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author HypherionSA
 * The main Discord Bot class. This controls everything surrounding the bot itself
 */
public class BotController {

    // Thread Execution Manager
    public static final ScheduledExecutorService taskManager = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    // Public instance of this class that can be called anywhere
    public static BotController INSTANCE;

    @Getter
    private final EventWaiter eventWaiter = new EventWaiter();
    @Getter
    private final Logger logger;

    // Required Variables
    private JDA _jda;
    private boolean shutdownCalled = false;

    /**
     * INTERNAL
     *
     * @param logger A constructed {@link Logger} that the bot will use
     */
    private BotController(Logger logger) {
        INSTANCE = this;
        this.logger = logger;

        File newConfigDir = new File("./config/simple-discord-link");
        newConfigDir.mkdirs();

        // Initialize Config
        new SDLinkConfig();

        // Initialize Account Storage
        DatabaseManager.initialize();

        // Initialize Webhook Clients
        WebhookManager.init();

        // Initialize Embeds
        EmbedManager.init();
    }

    /**
     * Construct a new instance of this class
     *
     * @param logger A constructed {@link Logger} that the bot will use
     */
    public static void newInstance(Logger logger) {
        new BotController(logger);
    }

    /**
     * Start the bot and handle all the startup work
     */
    public void initializeBot() {
        shutdownCalled = false;

        if (SDLinkConfig.INSTANCE == null || !SDLinkConfig.hasConfigLoaded) {
            logger.error("Failed to load config. Check your log for errors");
            return;
        }

        if (SDLinkConfig.INSTANCE.botConfig.botToken.isEmpty()) {
            logger.error("Missing bot token. Mod will be disabled. Please double check this in {}", SDLinkConfig.INSTANCE.getConfigPath());
            return;
        }

        if (!SDLinkConfig.INSTANCE.generalConfig.enabled) {
            logger.warn("Simple Discord Link is disabled. Not continuing");
            return;
        }

        try {
            String token = EncryptionUtil.INSTANCE.decrypt(SDLinkConfig.INSTANCE.botConfig.botToken);
            _jda = JDABuilder.createLight(
                            token,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setBulkDeleteSplittingEnabled(true)
                    .setEventManager(new ThreadedEventManager())
                    .build();

            // Setup Commands
            CommandClientBuilder clientBuilder = new CommandClientBuilder();
            clientBuilder.setOwnerId("354707828298088459");
            clientBuilder.setHelpWord("help");
            clientBuilder.useHelpBuilder(false);
            clientBuilder.setActivity(null);
            //clientBuilder.forceGuildOnly(750990873311051786L);

            CommandClient commandClient = clientBuilder.build();
            CommandManager.INSTANCE.register(commandClient);

            // Register Event Handlers
            _jda.addEventListener(commandClient, eventWaiter, new DiscordEventHandler());
            _jda.setAutoReconnect(true);

        } catch (Exception e) {
            logger.error("Failed to connect to discord", e);
        }
    }

    /**
     * Check if the bot is in a state to send messages to discord
     */
    public boolean isBotReady() {
        if (SDLinkConfig.INSTANCE == null)
            return false;

        if (shutdownCalled)
            return false;

        if (!SDLinkConfig.INSTANCE.generalConfig.enabled)
            return false;

        if (_jda == null)
            return false;

        if (_jda.getStatus() == JDA.Status.SHUTTING_DOWN || _jda.getStatus() == JDA.Status.SHUTDOWN)
            return false;

        return _jda.getStatus() == JDA.Status.CONNECTED;
    }

    /**
     * Shutdown the Bot
     */
    public void shutdownBot() {
        shutdownCalled = true;
        if (_jda != null) {
            _jda.shutdown();
        }

        WebhookManager.shutdown();

        // Workaround for Bot thread hanging after server shutdown
        taskManager.schedule(() -> {
            taskManager.shutdownNow();
            System.exit(0);
        }, 10, TimeUnit.SECONDS);
    }

    public JDA getJDA() {
        return this._jda;
    }

}
