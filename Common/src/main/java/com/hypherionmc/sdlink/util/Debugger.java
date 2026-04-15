package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import io.sentry.Sentry;
import org.slf4j.Logger;

import java.io.File;

public class Debugger {

    private final StackWalker STACK_WALKER = StackWalker.getInstance();
    private final Logger LOGGER = SDLinkConstants.LOGGER;
    private final File MARKER = new File("./sdlinkstorage/IAMDADEV");

    public static final Debugger INSTANCE = new Debugger();

    public void log(String message) {
        StackWalker.StackFrame frame = STACK_WALKER.walk(stream -> stream.skip(1).findFirst().orElse(null));

        if (frame == null || !MARKER.exists()) {
            return;
        }

        LOGGER.warn("[{}#{}:{}] " + message, frame.getClassName(), frame.getMethodName(), frame.getLineNumber());
    }

    public void log(String message, Object... args) {
        StackWalker.StackFrame frame = STACK_WALKER.walk(stream -> stream.skip(1).findFirst().orElse(null));

        if (frame == null || !MARKER.exists()) {
            return;
        }

        LOGGER.warn("[{}#{}:{}] " + message, frame.getClassName(), frame.getMethodName(), frame.getLineNumber(), args);
    }

    public static void setupSentry() {
        Sentry.init(options -> {
            options.setDsn("https://cbd223e80933420d92fc7c82a347de0b@sink.firstdark.dev/4");
            options.setEnvironment("experimental");

            options.setTracesSampleRate(0.0d);
            options.setAttachStacktrace(true);
            options.setSendDefaultPii(false);
        });
    }
}
