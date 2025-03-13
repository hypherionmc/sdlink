package com.hypherionmc.sdlink.core.config.impl.compat;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

public final class VanishCompat {

    @Path("sendFakeJoinLeaveMessage")
    @SpecComment("Should Fake Join/Leave message be sent when players vanish/unvanish")
    public boolean sendFakeJoinLeaveMessage = true;

}
