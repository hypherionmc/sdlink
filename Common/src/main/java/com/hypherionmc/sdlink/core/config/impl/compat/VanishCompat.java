package com.hypherionmc.sdlink.core.config.impl.compat;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;

public final class VanishCompat {

    @Path("sendFakeJoinLeaveMessage")
    @SpecComment("Should Fake Join/Leave message be sent when players vanish/unvanish")
    public boolean sendFakeJoinLeaveMessage = true;

}
