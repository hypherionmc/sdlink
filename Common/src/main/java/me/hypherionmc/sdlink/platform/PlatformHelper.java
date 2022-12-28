package me.hypherionmc.sdlink.platform;

import me.hypherionmc.sdlink.SDLinkConstants;
import me.hypherionmc.sdlink.platform.services.ModHelper;

import java.util.ServiceLoader;

public class PlatformHelper {

    public static final ModHelper MOD_HELPER = load(ModHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        SDLinkConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

}
