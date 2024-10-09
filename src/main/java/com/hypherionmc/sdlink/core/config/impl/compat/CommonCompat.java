package com.hypherionmc.sdlink.core.config.impl.compat;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

public class CommonCompat {

    @Path("vanish")
    @SpecComment("Should SDLink integrate with Vanish Mod")
    public boolean vanish = true;

    @Path("ftbessentials")
    @SpecComment("Should SDLink integrate with FTB Essentials")
    public boolean ftbessentials = true;

}
