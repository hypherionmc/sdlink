package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import org.slf4j.Logger;

public class Debugger {

    private final StackWalker STACK_WALKER = StackWalker.getInstance();
    private final Logger LOGGER = SDLinkConstants.LOGGER;

    public static final Debugger INSTANCE = new Debugger();

    public void log(String message) {
        log(message, new Object[0]);
    }

    public void log(String message, Object... args) {
        StackWalker.StackFrame frame = STACK_WALKER.walk(stream -> stream.skip(1).findFirst().orElse(null));

        if (frame == null || !SDLinkConfig.INSTANCE.generalConfig.debugging) {
            return;
        }

        LOGGER.error("[{}#{}:{}] " + message, frame.getClassName(), frame.getMethodName(), frame.getLineNumber(), args);
    }

}
