package com.hypherionmc.sdlink.core.experimental;

import com.hypherionmc.sdlink.core.discord.BotController;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExperimentalFeatures {

    public static final ExperimentalFeatures INSTANCE = new ExperimentalFeatures();

    public boolean RELAY_SERVER = false;
    public boolean IGNORE_CANCELLED_CHAT = false;

    public void loadFeatures() {
        File f = new File("./sdlinkstorage/IKNOWWHATIMDOING");

        if (!f.exists())
            return;

        try {
            Properties props = new Properties();
            props.load(new FileReader(f));

            RELAY_SERVER = Boolean.parseBoolean(props.getProperty("RELAY_SERVER", "false"));
            IGNORE_CANCELLED_CHAT = Boolean.parseBoolean(props.getProperty("IGNORE_CANCELLED_CHAT", "false"));

            BotController.INSTANCE.getLogger().warn("Experimental features are enabled");
            BotController.INSTANCE.getLogger().warn("RELAY_SERVER: {}", RELAY_SERVER);
            BotController.INSTANCE.getLogger().warn("IGNORE_CANCELLED_CHAT: {}", IGNORE_CANCELLED_CHAT);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to load Experimental Features", e);
        }
    }

}
