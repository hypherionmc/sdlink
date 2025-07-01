package com.hypherionmc.sdlink.core.config;

import com.hypherionmc.sdlink.core.config.impl.MessageIgnoreConfig;

public enum AppliesTo {
    DISCORD,
    MINECRAFT;

    public boolean appliesToChat(MessageIgnoreConfig.Ignore ignore) {
        return this == DISCORD && ignore.target.isChat();
    }

    public boolean appliesToUsername(MessageIgnoreConfig.Ignore ignore) {
        return this == DISCORD && ignore.target.isUsername();
    }

    public boolean appliesToConsole(MessageIgnoreConfig.Ignore ignore) {
        if (ignore.target == MessageIgnoreConfig.FilterTarget.CONSOLE)
            return true;

        return !ignore.ignoreConsole;
    }

    public boolean isMinecraft() {
        return this == MINECRAFT;
    }
}