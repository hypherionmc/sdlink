/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord;

import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.SDLinkRelayConfig;
import com.hypherionmc.sdlink.core.discord.commands.CommandManager;
import com.hypherionmc.sdlink.core.discord.events.DiscordEventHandler;
import com.hypherionmc.sdlink.core.experimental.ExperimentalFeatures;
import com.hypherionmc.sdlink.core.managers.*;
import com.hypherionmc.sdlink.core.relay.SDLinkRelayClient;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import com.hypherionmc.sdlink.util.ThreadedEventManager;
import com.hypherionmc.sdlinkrw.modules.cache.discord.WebhookCluster;
import com.hypherionmc.sdlinkrw.modules.editor.ConfigEditorClient;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author HypherionSA
 * The main Discord Bot class. This controls everything surrounding the bot itself
 */
public final class BotController {

    // Thread Execution Manager
    public final ExecutorService taskManager = Executors.newCachedThreadPool();
    public final ScheduledExecutorService updatesManager = Executors.newScheduledThreadPool(4);

    // Public instance of this class that can be called anywhere
    public static BotController INSTANCE;

    @Getter
    private final EventWaiter eventWaiter = new EventWaiter();
    @Getter
    public final Logger logger;

    @Getter
    private final SpamManager spamManager;

    // Required Variables
    private JDA _jda;
    private boolean shutdownCalled = false;

    /**
     * INTERNAL
     *
     * @param logger A constructed {@link Logger} that the bot will use
     */
    private BotController(Logger logger, boolean wasReload) {
        INSTANCE = this;
        this.logger = logger;

        File newConfigDir = new File("./config/simple-discord-link");
        newConfigDir.mkdirs();

        File newConfigBackup = new File(newConfigDir, "simple-discord-link.experimental");
        File currentConfig = new File(newConfigDir, "simple-discord-link.toml");

        if (newConfigBackup.exists()) {
            logger.info("Found backed up experimental config. Restoring...");
            try {
                FileUtils.moveFile(currentConfig, new File(currentConfig.getAbsolutePath().replace(".toml", ".legacy")));
                FileUtils.moveFile(newConfigBackup, currentConfig);
            } catch (Exception e) {
                logger.error("Failed to restore config", e);
            }
        }

        ExperimentalFeatures.INSTANCE.loadFeatures();

        // Initialize Config
        new SDLinkConfig(wasReload);
        new SDLinkCompatConfig(wasReload);

        // TODO: Remove when released
        // Experimental Feature
        if (ExperimentalFeatures.INSTANCE.RELAY_SERVER) {
            new SDLinkRelayConfig(wasReload);
        }

        // Initialize Account Storage
        DatabaseManager.INSTANCE.initialize();

        // Initialize Embeds
        EmbedManager.init();

        // Initialize Hidden players
        HiddenPlayersManager.INSTANCE.loadHiddenPlayers();

        // Initialize spam detector with the following limits: 5 Messages in 2 seconds, with an expiration of 2 minutes
        spamManager = new SpamManager(5, 2000, 120000, updatesManager);
    }

    /**
     * Construct a new instance of this class
     *
     * @param logger A constructed {@link Logger} that the bot will use
     */
    public static void newInstance(Logger logger) {
        new BotController(logger, false);
    }

    public static void reloadInstance(boolean isReload) {
        BotController.INSTANCE.shutdownBot(isReload);
        new BotController(INSTANCE.logger, true);
        BotController.INSTANCE.initializeBot();
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

            // Setup Commands
            CommandClientBuilder clientBuilder = new CommandClientBuilder();
            clientBuilder.setOwnerId("354707828298088459");
            clientBuilder.setHelpWord("help");
            clientBuilder.useHelpBuilder(false);
            clientBuilder.setActivity(null);
            //clientBuilder.forceGuildOnly(750990873311051786L);

            CommandClient commandClient = clientBuilder.build();
            CommandManager.INSTANCE.register(commandClient);

            _jda = JDABuilder.createLight(
                            token,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_EXPRESSIONS
                    )
                    .addEventListeners(commandClient, eventWaiter, new DiscordEventHandler())
                    .setAutoReconnect(true)
                    .enableCache(CacheFlag.EMOJI)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setBulkDeleteSplittingEnabled(true)
                    .setEventManager(new ThreadedEventManager())
                    .build();
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
    public void shutdownBot(boolean isReload) {
        try {
            shutdownCalled = true;
            if (_jda != null) {
                List<Object> listeners = _jda.getRegisteredListeners();
                listeners.forEach(l -> _jda.removeEventListener(l));
                _jda.shutdownNow();
            }

            WebhookCluster.INSTANCE.shutdown();
            taskManager.shutdownNow();
            updatesManager.shutdownNow();
        } catch (IllegalStateException ignored) {
            // This is sometimes triggered on paper, but works as expected. So we just ignore it
        } catch (Exception e) {
            logger.error("Failed to shutdown bot.", e);
        }

        if (!isReload) {
            try {
                ConfigEditorClient.INSTANCE.closeServer();

                // TODO: Remove on release
                if (ExperimentalFeatures.INSTANCE.RELAY_SERVER)
                    SDLinkRelayClient.INSTANCE.closeServer(true);
            } catch (Exception ignored) {}
        }
    }

    public JDA getJDA() {
        return this._jda;
    }

}
