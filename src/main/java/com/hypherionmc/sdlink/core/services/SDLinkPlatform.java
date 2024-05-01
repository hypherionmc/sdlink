/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.services;

import com.hypherionmc.craterlib.utils.InternalServiceUtil;
import com.hypherionmc.sdlink.core.services.helpers.IMinecraftHelper;

/**
 * @author HypherionSA
 * Service loader for library services
 */
public class SDLinkPlatform {

    public static IMinecraftHelper minecraftHelper = InternalServiceUtil.load(IMinecraftHelper.class);

}
