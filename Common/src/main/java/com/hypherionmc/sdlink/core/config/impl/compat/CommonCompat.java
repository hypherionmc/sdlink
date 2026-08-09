package com.hypherionmc.sdlink.core.config.impl.compat;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;

public final class CommonCompat {

    @Path("ignoreCancelled")
    @SpecComment("Ignore cancelled chat events. Fixes compat with some mods like FTB Teams")
    public boolean ignoreCancelled = false;

    @Path("vanish")
    @SpecComment("Should SDLink integrate with Vanish Mod")
    public boolean vanish = true;

    @Path("ftbessentials")
    @SpecComment("Should SDLink integrate with FTB Essentials")
    public boolean ftbessentials = true;

    @Path("ftbranks")
    @SpecComment("Should SDLink integrate with FTB Ranks")
    public boolean ftbranks = true;

    @Path("luckperms")
    @SpecComment("Should SDLink integrate with Luckperms (Group Syncing only)")
    public boolean luckperms = true;

    @Path("playerroles")
    @SpecComment("Should SDLink integrate with Player Roles")
    public boolean playerroles = true;

    @Path("cobblemonguilds")
    @SpecComment("Should SDLink integrate with Cobblemon Guilds")
    public boolean cobblemonguilds = true;

    @Path("ftbteams_chat")
    @SpecComment("Should chats from the /ftbteams chat command be relayed to discord")
    public boolean ftbteams_chat = false;

}
