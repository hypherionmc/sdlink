/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config;

import com.hypherionmc.craterlib.core.config.AbstractConfig;
import com.hypherionmc.craterlib.core.config.ConfigController;
import com.hypherionmc.craterlib.libs.moonconfig.core.CommentedConfig;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.ObjectConverter;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.craterlib.libs.moonconfig.core.file.CommentedFileConfig;
import com.hypherionmc.sdlink.core.config.impl.*;
import com.hypherionmc.sdlink.core.config.impl.channels.ChannelConfig;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache;
import com.hypherionmc.sdlinkrw.modules.translations.TranslationManager;
import com.hypherionmc.sdlinkrw.util.EncryptionUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * @author HypherionSA
 * The main mod config Structure
 */
public final class SDLinkConfig extends AbstractConfig<SDLinkConfig> {

    // DO NOT REMOVE TRANSIENT HERE... OTHERWISE, THE STUPID CONFIG LIBRARY
    // WILL TRY TO WRITE THESE TO THE CONFIG
    public transient static SDLinkConfig INSTANCE;
    public transient static int configVer = 43;
    public transient static boolean hasConfigLoaded = false;
    public transient static boolean wasReload = false;

    @Path("general")
    @SpecComment("General Mod Config. Check out https://sdlink.fdd-docs.com for help on configuring this")
    public GeneralConfigSettings generalConfig = new GeneralConfigSettings();

    @Path("botConfig")
    @SpecComment("Config specific to the discord bot")
    public BotConfigSettings botConfig = new BotConfigSettings();

    @Path("chat")
    @SpecComment("Configure which types of messages are delivered to Minecraft/Discord")
    public ChatSettingsConfig chatConfig = new ChatSettingsConfig();

    @Path("channels")
    @SpecComment("Manage Channels/and Messages")
    public ChannelConfig channels = new ChannelConfig();

    @Path("accessControl")
    @SpecComment("Manage access to your server, similar to whitelisting")
    public AccessControl accessControl = new AccessControl();

    @Path("minecraftCommands")
    @SpecComment("Execute Minecraft commands in Discord")
    public MinecraftCommands linkedCommands = new MinecraftCommands();

    @Path("filtering")
    @SpecComment("Configure message/username filtering for discord messages")
    public MessageIgnoreConfig ignoreConfig = new MessageIgnoreConfig();

    @Path("triggerCommands")
    @SpecComment("Run Minecraft commands when discord roles changes. Requires Access Control to be enabled")
    public TriggerCommandsConfig triggerCommands = new TriggerCommandsConfig();

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

        this.configReloaded();
        performEncryption(wasReload);
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
        updateConfigValues(config, newConfig, newConfig, "");
        newConfig.set("general.configVersion", configVer);

        try {
            FileUtils.copyFile(getConfigPath(), new File(getConfigPath().getAbsolutePath().replace(".toml", config.getInt("general.configVersion") < 40 ? ".legacy" : ".old")));
        } catch (IOException e) {
            SDLinkConstants.LOGGER.warn("Failed to create config backup.", e);
        }

        newConfig.save();
        newConfig.close();
        config.close();
    }

    @Override
    public void configReloaded() {
        INSTANCE = readConfig(this);
        hasConfigLoaded = true;
        SDLCache.INSTANCE.reloadChannelConfigCache();

        try {
            TranslationManager.INSTANCE.loadTranslations(SDLinkConfig.INSTANCE.generalConfig.language);
        } catch (Exception ignored) {} // This sometimes fails randomly
    }

    /**
     * Apply encryption to Bot-Token and Webhook URLS
     */
    private void performEncryption(boolean wasReload) {
        CommentedFileConfig oldConfig = CommentedFileConfig.builder(this.getConfigPath()).sync().build();
        oldConfig.load();

        String botToken = oldConfig.getOrElse("botConfig.botToken", "");

        if (!botToken.isEmpty()) {
            botToken = EncryptionUtil.INSTANCE.encrypt(botToken);
            oldConfig.set("botConfig.botToken", botToken);
        }

        oldConfig.save();
        oldConfig.close();

        if (!wasReload) {
            ConfigController.register_config(this);
        }
        this.configReloaded();
    }

    private void updateConfigValues(CommentedConfig oldConfig, CommentedConfig newConfig, CommentedConfig outputConfig, String subKey) {
        int ver = oldConfig.getInt("general.configVersion");

        // TODO: Move this to its own handler
        newConfig.valueMap().forEach((key, value) -> {
            String finalKey = subKey + (subKey.isEmpty() ? "" : ".") + key;

            if (ver < 40) {
                if (finalKey.equalsIgnoreCase("channelsAndWebhooks.channels.chatChannelID")) {
                    outputConfig.set("botConfig.defaultChannels.default_chat", Collections.singletonList(oldConfig.get(finalKey)));
                    return;
                }

                if (finalKey.equalsIgnoreCase("channelsAndWebhooks.channels.eventsChannelID")) {
                    outputConfig.set("botConfig.defaultChannels.default_event", Collections.singletonList(oldConfig.get(finalKey)));
                    return;
                }

                if (finalKey.equalsIgnoreCase("channelsAndWebhooks.channels.consoleChannelID")) {
                    outputConfig.set("botConfig.defaultChannels.default_console", Collections.singletonList(oldConfig.get(finalKey)));
                    return;
                }
            }

            if (ver < 41) {
                if (finalKey.equalsIgnoreCase("channelsAndWebhooks.channels.chatChannelID")) {
                    outputConfig.set("botConfig.defaultChannels.default_chat", oldConfig.get(finalKey));
                    return;
                }

                if (finalKey.equalsIgnoreCase("channelsAndWebhooks.channels.eventsChannelID")) {
                    outputConfig.set("botConfig.defaultChannels.default_event", oldConfig.get(finalKey));
                    return;
                }

                if (finalKey.equalsIgnoreCase("channelsAndWebhooks.channels.consoleChannelID")) {
                    outputConfig.set("botConfig.defaultChannels.default_console", oldConfig.get(finalKey));
                    return;
                }
            }

            if (ver < 21) {
                if (finalKey.equalsIgnoreCase("botConfig.botStatus")) {
                    outputConfig.set(finalKey, Collections.singletonList(oldConfig.get(finalKey)));
                    return;
                }
            }

            if (ver < 27) {
                if (finalKey.equalsIgnoreCase("accessControl.verifiedRole")) {
                    if (!(oldConfig.get(finalKey) instanceof String)) return;
                    outputConfig.set(finalKey, oldConfig.get(finalKey).toString().trim().isEmpty() ? Collections.emptyList() : Collections.singletonList(oldConfig.get(finalKey)));
                    return;
                }

                if (finalKey.equalsIgnoreCase("chat.advancementMessages") || finalKey.equalsIgnoreCase("chat.deathMessages")) {
                    if (!(oldConfig.get(finalKey) instanceof Boolean)) return;
                    outputConfig.set(finalKey, ((boolean) oldConfig.get(finalKey)) ? GameRuleBoolean.ALWAYS : GameRuleBoolean.NEVER);
                    return;
                }
            }

            if (ver == 27 || ver <= 26) {
                if (finalKey.equalsIgnoreCase("messageFormatting.advancements")) {
                    outputConfig.set(finalKey, oldConfig.get("messageFormatting.achievements"));
                    return;
                }

                if (finalKey.equalsIgnoreCase("ignoredMessages")) {
                    outputConfig.set("filtering", oldConfig.get("ignoredMessages"));
                    outputConfig.set("filtering.enabled", oldConfig.get("ignoredMessages.enabled"));
                    return;
                }
            }

            if (value instanceof CommentedConfig commentedConfig) {
                this.updateConfigValues(oldConfig, commentedConfig, outputConfig, finalKey);
            } else {
                outputConfig.set(finalKey, oldConfig.contains(finalKey) ? oldConfig.get(finalKey) : value);
            }
        });
    }
}
