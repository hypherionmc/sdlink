package com.hypherionmc.sdlink.core.config;

import com.hypherionmc.craterlib.core.config.AbstractConfig;
import com.hypherionmc.craterlib.core.config.ConfigController;
import com.hypherionmc.craterlib.core.config.formats.TomlConfigFormat;
import com.hypherionmc.sdlink.core.config.impl.RelayMessageConfig;
import com.hypherionmc.sdlink.core.config.impl.RelayServerConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.relay.SDLinkRelayClient;
import org.apache.commons.io.FileUtils;
import shadow.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;
import shadow.hypherionmc.moonconfig.core.file.CommentedFileConfig;

import java.io.File;
import java.io.IOException;

public final class SDLinkRelayConfig extends AbstractConfig<SDLinkRelayConfig> {

    // DO NOT REMOVE TRANSIENT HERE... OTHERWISE, THE STUPID CONFIG LIBRARY
    // WILL TRY TO WRITE THESE TO THE CONFIG
    public transient static SDLinkRelayConfig INSTANCE;
    public transient static int configVer = 2;
    public transient static boolean hasConfigLoaded = false;
    public transient static boolean wasReload = false;

    @Path("configVersion")
    @SpecComment("INTERNAL. DO NOT TOUCH")
    public int configVersion = configVer;

    @Path("relayServer")
    @SpecComment("General Relay Server Config")
    public RelayServerConfig relayServer = new RelayServerConfig();

    @Path("messageConfig")
    @SpecComment("Message config for relays")
    public RelayMessageConfig messageConfig = new RelayMessageConfig();

    public SDLinkRelayConfig() {
        this(false);
    }

    public SDLinkRelayConfig(boolean wasReload) {
        super("sdlink", "simple-discord-link", "simple-discord-relay");
        SDLinkRelayConfig.wasReload = wasReload;
        registerAndSetup(this);
    }

    @Override
    public void registerAndSetup(SDLinkRelayConfig config) {
        if (this.getConfigPath().exists() && this.getConfigPath().length() >= 2L) {
            this.migrateConfig(config);
        } else {
            this.saveConfig(config);
        }

        if (!wasReload) {
            ConfigController.register_config(this);
        }

        this.configReloaded();
    }

    @Override
    public void migrateConfig(SDLinkRelayConfig conf) {
        CommentedFileConfig config = CommentedFileConfig.builder(getConfigPath()).sync().build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(getConfigPath()).sync().build();
        config.load();

        if (config.getInt("configVersion") == configVer) {
            newConfig.close();
            config.close();
            return;
        }

        new ObjectConverter().toConfig(conf, newConfig);
        ((TomlConfigFormat<SDLinkRelayConfig>)this.getConfigFormat()).updateConfigValues(config, newConfig, newConfig, "");
        newConfig.set("configVersion", configVer);

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
        SDLinkRelayClient.INSTANCE.openConnection();
    }
}
