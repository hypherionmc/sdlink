/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.core.config.AvatarType;

/**
 * @author HypherionSA
 * Config Structure to control what types of messages are supported by the mod
 */
public final class ChatSettingsConfig {

    @Path("useLinkedNames")
    @SpecComment("Use linked account names in Discord/Minecraft messages, instead of the default ones")
    public boolean useLinkedNames = true;

    @Path("useLinkedAvatar")
    @SpecComment("Use linked account avatar in Discord messages, instead of custom avatar")
    public boolean useLinkedAvatar = true;

    @Path("formatting")
    @SpecComment("Convert Discord to MC, and MC to Discord Formatting")
    public boolean formatting = true;

    @Path("customAvatarService")
    @SpecComment("Add your own Avatar service URL here. Use {uuid} to replace the player ID in the URL")
    public String customAvatarService = "https://crafatar.com/avatars/{uuid}";

    @Path("playerAvatarType")
    @SpecComment("The type of image to use as the player icon in messages. Valid entries are: AVATAR, HEAD, BODY, COMBO, CUSTOM")
    public AvatarType playerAvatarType = AvatarType.HEAD;

}
