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

    public void loadFeatures() {
        File f = new File("./sdlinkstorage/IKNOWWHATIMDOING");

        if (!f.exists())
            return;

        try {
            Properties props = new Properties();
            props.load(new FileReader(f));

            RELAY_SERVER = Boolean.parseBoolean(props.getProperty("RELAY_SERVER", "false"));
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to load Experimental Features", e);
        }
    }

}
