package com.hypherionmc.sdlink.core.config.impl.compat;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

public final class CommonCompat {

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

    @Path("ftbteams_chat")
    @SpecComment("Should chats from the /ftbteams chat command be relayed to discord")
    public boolean ftbteams_chat = false;

}
