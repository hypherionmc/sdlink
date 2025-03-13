package com.hypherionmc.sdlink.core.config.impl.compat;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

public final class PlayerReviveCompat {

    @Path("enabled")
    @SpecComment("Should integration with Player Revive Mod be enabled")
    public boolean enabled = true;

    @Path("reviveWaitingMessage")
    @SpecComment("Message to be sent to discord, while the player is waiting to be revived")
    public String reviveWaitingMessage = "%player% is bleeding out and may need your help";

    @Path("revivedMessage")
    @SpecComment("Message to be sent to discord, when the player is revived")
    public String revivedMessage = "%player% has been revived";

    @Path("playerBledOutMessage")
    @SpecComment("Message to be sent to discord, when the player dies for real")
    public String playerBledOutMessage = "%player% %message%";

}
