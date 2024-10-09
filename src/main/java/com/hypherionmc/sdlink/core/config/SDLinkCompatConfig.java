package com.hypherionmc.sdlink.core.config;

import com.hypherionmc.craterlib.core.config.AbstractConfig;
import com.hypherionmc.craterlib.core.config.ConfigController;
import com.hypherionmc.craterlib.core.config.annotations.NoConfigScreen;
import com.hypherionmc.craterlib.core.config.formats.TomlConfigFormat;
import com.hypherionmc.sdlink.core.config.impl.compat.CommonCompat;
import com.hypherionmc.sdlink.core.config.impl.compat.MaintenanceModeCompat;
import com.hypherionmc.sdlink.core.discord.BotController;
import org.apache.commons.io.FileUtils;
import shadow.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;
import shadow.hypherionmc.moonconfig.core.file.CommentedFileConfig;

import java.io.File;
import java.io.IOException;

@NoConfigScreen
public class SDLinkCompatConfig extends AbstractConfig<SDLinkCompatConfig> {

    // DO NOT REMOVE TRANSIENT HERE... OTHERWISE, THE STUPID CONFIG LIBRARY
    // WILL TRY TO WRITE THESE TO THE CONFIG
    public transient static SDLinkCompatConfig INSTANCE;
    public transient static int configVer = 1;
    public transient static boolean hasConfigLoaded = false;
    public transient static boolean wasReload = false;

    @Path("common")
    @SpecComment("Disable/Enable basic integrations")
    public CommonCompat common = new CommonCompat();

    @Path("maintenance_mode")
    @SpecComment("Manage Maintenance Mode integration")
    public MaintenanceModeCompat maintenanceModeCompat = new MaintenanceModeCompat();

    public SDLinkCompatConfig() {
        this(false);
    }

    public SDLinkCompatConfig(boolean wasReload) {
        super("sdlink", "simple-discord-link", "simple-discord-compat");
        SDLinkCompatConfig.wasReload = wasReload;
        registerAndSetup(this);
    }

    @Override
    public void registerAndSetup(SDLinkCompatConfig config) {
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
    public void migrateConfig(SDLinkCompatConfig conf) {
        CommentedFileConfig config = CommentedFileConfig.builder(getConfigPath()).sync().build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(getConfigPath()).sync().build();
        config.load();

        if (config.getInt("configVersion") == configVer) {
            newConfig.close();
            config.close();
            return;
        }

        new ObjectConverter().toConfig(conf, newConfig);
        ((TomlConfigFormat<SDLinkCompatConfig>)this.getConfigFormat()).updateConfigValues(config, newConfig, newConfig, "");
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
    }
}
