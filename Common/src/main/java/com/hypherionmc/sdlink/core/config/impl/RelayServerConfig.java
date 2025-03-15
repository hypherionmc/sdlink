package com.hypherionmc.sdlink.core.config.impl;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

public final class RelayServerConfig {

    @Path("enabled")
    @SpecComment("Enable or Disable the relay server")
    public boolean enabled = false;

    @Path("relayServerUrl")
    @SpecComment("The Relay Server to connect to. If you use an IP address, include the port, like 127.0.0.1:1234")
    public String relayServerUrl = "sdlinkrelay.firstdark.dev";

    @Path("relayToken")
    @SpecComment("A secret, password if you will, to connect your servers with. This has to match on all connected servers")
    public String relayToken = "";

}
