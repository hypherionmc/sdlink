/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.messaging;

import com.hypherionmc.sdlink.util.translations.SDText;
import lombok.Getter;

/**
 * @author HypherionSA
 * Helper Class to return the result of interactions between Discord and Minecraft
 */
public final class Result {

    private final Type type;
    @Getter
    private final String message;
    private Result(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public static Result success(SDText text) {
        return Result.success(text.toString());
    }

    public static Result success(String message) {
        return new Result(Type.SUCCESS, message);
    }

    public static Result error(SDText text) {
        return Result.error(text.toString());
    }

    public static Result error(String message) {
        return new Result(Type.ERROR, message);
    }

    public boolean isError() {
        return this.type == Type.ERROR;
    }

    enum Type {
        ERROR,
        SUCCESS
    }

}
