/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

import java.util.ArrayList;
import java.util.List;

public class MinecraftCommands {

    @Path("enabled")
    @SpecComment("Allow executing Minecraft commands from Discord")
    public boolean enabled = false;

    @Path("prefix")
    @SpecComment("Command Prefix. For example ?weather clear")
    public String prefix = "?";

    @Path("permissions")
    @SpecComment("List of command permissions")
    public List<Command> permissions = new ArrayList<>();

    public static class Command {
        public String role = "0";

        public List<String> commands = new ArrayList<>();

        public int permissionLevel = 1;
    }

}
