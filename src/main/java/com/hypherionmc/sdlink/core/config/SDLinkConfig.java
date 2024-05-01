/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config;

import com.hypherionmc.craterlib.core.config.ConfigController;
import com.hypherionmc.craterlib.core.config.ModuleConfig;
import com.hypherionmc.craterlib.core.config.annotations.NoConfigScreen;
import com.hypherionmc.sdlink.core.config.impl.*;
import com.hypherionmc.sdlink.core.util.EncryptionUtil;
import shadow.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;
import shadow.hypherionmc.moonconfig.core.file.CommentedFileConfig;

/**
 * @author HypherionSA
 * The main mod config Structure
 */
@NoConfigScreen
public class SDLinkConfig extends ModuleConfig {

    // DO NOT REMOVE TRANSIENT HERE... OTHERWISE, THE STUPID CONFIG LIBRARY
    // WILL TRY TO WRITE THESE TO THE CONFIG
    public transient static SDLinkConfig INSTANCE;
    public transient static int configVer = 14;
    public transient static boolean hasConfigLoaded = false;

    @Path("general")
    @SpecComment("General Mod Config")
    public GeneralConfigSettings generalConfig = new GeneralConfigSettings();

    @Path("botConfig")
    @SpecComment("Config specific to the discord bot")
    public BotConfigSettings botConfig = new BotConfigSettings();

    @Path("channelsAndWebhooks")
    @SpecComment("Config relating to the discord channels and webhooks to use with the mod")
    public ChannelWebhookConfig channelsAndWebhooks = new ChannelWebhookConfig();

    @Path("chat")
    @SpecComment("Configure which types of messages are delivered to Minecraft/Discord")
    public ChatSettingsConfig chatConfig = new ChatSettingsConfig();

    @Path("messageFormatting")
    @SpecComment("Change the format in which messages are displayed")
    public MessageFormatting messageFormatting = new MessageFormatting();

    @Path("messageDestinations")
    @SpecComment("Change in which channel messages appear")
    public MessageChannelConfig messageDestinations = new MessageChannelConfig();

    @Path("accessControl")
    @SpecComment("Manage access to your server, similar to whitelisting")
    public AccessControl accessControl = new AccessControl();

    @Path("minecraftCommands")
    @SpecComment("Execute Minecraft commands in Discord")
    public MinecraftCommands linkedCommands = new MinecraftCommands();

    @Path("ignoredMessages")
    @SpecComment("Configure messages that will be ignored when relaying to discord")
    public MessageIgnoreConfig ignoreConfig = new MessageIgnoreConfig();

    public SDLinkConfig() {
        super("sdlink", "simple-discord-link", "simple-discord-link");
        registerAndSetup(this);
    }

    @Override
    public void registerAndSetup(ModuleConfig config) {
        if (this.getConfigPath().exists() && this.getConfigPath().length() >= 2L) {
            this.migrateConfig(config);
        } else {
            this.saveConfig(config);
        }

        performEncryption();
        ConfigController.register_config(this);
        this.configReloaded();
    }

    @Override
    public void migrateConfig(ModuleConfig conf) {
        CommentedFileConfig config = CommentedFileConfig.builder(getConfigPath()).build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(getConfigPath()).build();
        config.load();

        if (config.getInt("general.configVersion") == configVer) {
            newConfig.close();
            config.close();
            return;
        }

        new ObjectConverter().toConfig(conf, newConfig);
        this.updateConfigValues(config, newConfig, newConfig, "");
        newConfig.set("general.configVersion", configVer);
        newConfig.save();

        newConfig.close();
        config.close();
    }

    @Override
    public void configReloaded() {
        INSTANCE = loadConfig(this);
        hasConfigLoaded = true;
    }

    /**
     * Apply encryption to Bot-Token and Webhook URLS
     */
    private void performEncryption() {
        CommentedFileConfig oldConfig = CommentedFileConfig.builder(this.getConfigPath()).build();
        oldConfig.load();

        String botToken = oldConfig.getOrElse("botConfig.botToken", "");
        String chatWebhook = oldConfig.getOrElse("channelsAndWebhooks.webhooks.chatWebhook", "");
        String eventsWebhook = oldConfig.getOrElse("channelsAndWebhooks.webhooks.eventsWebhook", "");
        String consoleWebhook = oldConfig.getOrElse("channelsAndWebhooks.webhooks.consoleWebhook", "");

        if (!botToken.isEmpty()) {
            botToken = EncryptionUtil.INSTANCE.encrypt(botToken);
            oldConfig.set("botConfig.botToken", botToken);
        }

        if (!chatWebhook.isEmpty()) {
            chatWebhook = EncryptionUtil.INSTANCE.encrypt(chatWebhook);
            oldConfig.set("channelsAndWebhooks.webhooks.chatWebhook", chatWebhook);
        }

        if (!eventsWebhook.isEmpty()) {
            eventsWebhook = EncryptionUtil.INSTANCE.encrypt(eventsWebhook);
            oldConfig.set("channelsAndWebhooks.webhooks.eventsWebhook", eventsWebhook);
        }

        if (!consoleWebhook.isEmpty()) {
            consoleWebhook = EncryptionUtil.INSTANCE.encrypt(consoleWebhook);
            oldConfig.set("channelsAndWebhooks.webhooks.consoleWebhook", consoleWebhook);
        }

        oldConfig.save();
        oldConfig.close();
    }
}
