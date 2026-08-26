package com.hypherionmc.sdlink.core.config;

public enum GameRuleBoolean {
    ALWAYS,
    NEVER,
    GAMERULE;

    public boolean isTrue() {
        return this == ALWAYS || this == GAMERULE;
    }

    public boolean isFalse() {
        return this == NEVER;
    }

    public boolean followGameRule() {
        return this == GAMERULE;
    }
}
