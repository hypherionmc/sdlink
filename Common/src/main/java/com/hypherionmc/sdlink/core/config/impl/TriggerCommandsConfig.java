package com.hypherionmc.sdlink.core.config.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;

import java.util.ArrayList;
import java.util.List;

public final class TriggerCommandsConfig {

    @Path("enabled")
    @SpecComment("Should any of the below commands be executed when a role changes")
    public boolean enabled = false;

    @Path("roleAdded")
    @SpecComment("Commands to run when roles are added")
    public List<TriggerHolder> roleAdded = new ArrayList<>();

    @Path("roleRemoved")
    @SpecComment("Commands to run when roles are removed")
    public List<TriggerHolder> roleRemoved = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class TriggerHolder {

        @Path("discordRole")
        public String discordRole = "";

        @Path("minecraftCommand")
        public List<String> minecraftCommand = new ArrayList<>();
    }

}
