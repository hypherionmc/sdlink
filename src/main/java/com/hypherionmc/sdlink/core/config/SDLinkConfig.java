/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config;

import com.hypherionmc.craterlib.core.config.AbstractConfig;
import com.hypherionmc.craterlib.core.config.ConfigController;
import com.hypherionmc.craterlib.core.config.annotations.NoConfigScreen;
import com.hypherionmc.craterlib.core.config.formats.TomlConfigFormat;
import com.hypherionmc.sdlink.core.config.impl.*;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import org.apache.commons.io.FileUtils;
import shadow.hypherionmc.moonconfig.core.CommentedConfig;
import shadow.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;
import shadow.hypherionmc.moonconfig.core.file.CommentedFileConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author HypherionSA
 * The main mod config Structure
 */
@NoConfigScreen
public class SDLinkConfig extends AbstractConfig<SDLinkConfig> {

    // DO NOT REMOVE TRANSIENT HERE... OTHERWISE, THE STUPID CONFIG LIBRARY
    // WILL TRY TO WRITE THESE TO THE CONFIG
    public transient static SDLinkConfig INSTANCE;
    public transient static int configVer = 19;
    public transient static boolean hasConfigLoaded = false;
    public transient static boolean wasReload = false;

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

    public SDLinkConfig(boolean wasReload) {
        super("sdlink", "simple-discord-link", "simple-discord-link");
        SDLinkConfig.wasReload = wasReload;
        registerAndSetup(this);
    }

    public SDLinkConfig() {
        this(false);
    }

    @Override
    public void registerAndSetup(SDLinkConfig config) {
        if (this.getConfigPath().exists() && this.getConfigPath().length() >= 2L) {
            this.migrateConfig(config);
        } else {
            this.saveConfig(config);
        }

        performEncryption();
        if (!wasReload) {
            ConfigController.register_config(this);
        }
        this.configReloaded();
    }

    @Override
    public void migrateConfig(SDLinkConfig conf) {
        CommentedFileConfig config = CommentedFileConfig.builder(getConfigPath()).sync().build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(getConfigPath()).sync().build();
        config.load();

        if (config.getInt("general.configVersion") == configVer) {
            newConfig.close();
            config.close();
            return;
        }

        new ObjectConverter().toConfig(conf, newConfig);
        ((TomlConfigFormat<SDLinkConfig>)this.getConfigFormat()).updateConfigValues(config, newConfig, newConfig, "");
        newConfig.set("general.configVersion", configVer);
        try {
            FileUtils.copyFile(getConfigPath(), new File(getConfigPath().getAbsolutePath().replace(".toml", ".old")));
        } catch (IOException e) {
            BotController.INSTANCE.getLogger().warn("Failed to create config backup.", e);
        }
        newConfig.save();

        newConfig.close();
        config.close();
    }

    @Override
    public void configReloaded() {
        INSTANCE = readConfig(this);
        hasConfigLoaded = true;
    }

    /**
     * Apply encryption to Bot-Token and Webhook URLS
     */
    private void performEncryption() {
        CommentedFileConfig oldConfig = CommentedFileConfig.builder(this.getConfigPath()).sync().build();
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

        for (Map.Entry<MessageType, MessageChannelConfig.DestinationObject> d : CacheManager.messageDestinations.entrySet()) {
            if (!d.getValue().channel.isOverride() || d.getValue().override == null || !d.getValue().override.startsWith("http:"))
                continue;

            String url = d.getValue().override;
            encryptOverrideUrls(d.getKey().name().toLowerCase(), oldConfig, url);
        }

        oldConfig.save();
        oldConfig.close();
    }

    private void encryptOverrideUrls(String key, CommentedFileConfig oldConfig, String url) {
        oldConfig.set("messageDestinations." + key + ".override", "webhook:" + EncryptionUtil.INSTANCE.encrypt(url));
    }
}
