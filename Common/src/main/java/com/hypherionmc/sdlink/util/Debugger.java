package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import org.slf4j.Logger;

import java.util.Optional;

public class Debugger {

    private final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private final Logger LOGGER = SDLinkConstants.LOGGER;

    public static final Debugger INSTANCE = new Debugger();

    public void log(String message) {
        log(message, new Object[0]);
    }

    public void log(String message, Object... args) {
        if (!SDLinkConfig.INSTANCE.generalConfig.debugging) return;

        Optional<StackWalker.StackFrame> caller = STACK_WALKER
                .walk(stream -> stream.filter(f -> !f.getClassName().equals(getClass().getName())).findFirst());

        if (caller.isEmpty()) {
            return;
        }

        StackWalker.StackFrame frame = caller.get();

        Object[] allArgs = new Object[args.length + 3];
        allArgs[0] = frame.getDeclaringClass().getSimpleName();
        allArgs[1] = frame.getMethodName();
        allArgs[2] = frame.getLineNumber();
        System.arraycopy(args, 0, allArgs, 3, args.length);

        LOGGER.error("[{}#{}:{}] " + message, allArgs);
    }

}
