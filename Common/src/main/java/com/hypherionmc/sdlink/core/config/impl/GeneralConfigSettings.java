/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;

/**
 * @author HypherionSA
 * General Mod Settings config Structure
 */
public final class GeneralConfigSettings {

    @Path("enabled")
    @SpecComment("Should the mod be enabled or not")
    public boolean enabled = true;

    @Path("debugging")
    @SpecComment("Enable Additional Logging. Used for Fault Finding. WARNING: CAUSES LOG SPAM!")
    public boolean debugging = false;

    @Path("language")
    @SpecComment("The active language to use for built in messages. Defaults to en_us if a language is not found")
    public String language = "en_us";

    @Path("useRemoteEditor")
    @SpecComment("Use a remote instance of the bundled web interface. Useful if you cannot open your own port for it. Will take effect after a restart")
    public boolean useRemoteEditor = true;

    @Path("configEditorPort")
    @SpecComment("The port to use when running the web interface in local mode. Will take effect after a restart")
    public int configEditorPort = 7000;

    @Path("configVersion")
    @SpecComment("Internal version control. DO NOT TOUCH!")
    public int configVersion = SDLinkConfig.configVer;
}
