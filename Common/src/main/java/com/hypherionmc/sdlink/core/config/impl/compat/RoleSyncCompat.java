package com.hypherionmc.sdlink.core.config.impl.compat;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public final class RoleSyncCompat {

    @Path("syncToMinecraft")
    @SpecComment("Sync Groups/Ranks to Minecraft from Discord Roles")
    public boolean syncToMinecraft = false;

    @Path("syncToDiscord")
    @SpecComment("Sync Groups/Ranks to Discord roles from Minecraft")
    public boolean syncToDiscord = false;

    @Path("syncs")
    @SpecComment("List of Ranks and Roles that will be synced. Check the wiki on how to configure this")
    public List<Sync> syncs = new ArrayList<>();

    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class Sync {
        public String rank;
        public String role;
    }

}
