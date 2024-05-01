/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.accounts;

import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import lombok.Getter;

/**
 * @author HypherionSA
 * Represents a Message Author for messages sent from Minecraft to Discord
 */
@Getter
public class DiscordAuthor {

    // User used for Server Messages
    public static final DiscordAuthor SERVER = new DiscordAuthor(SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName, SDLinkConfig.INSTANCE.channelsAndWebhooks.serverAvatar, "server", true, "");

    private final String displayName;
    private final String avatar;
    private final boolean isServer;
    private String username;
    private String uuid;
    private BridgedGameProfile profile = null;
    String realPlayerAvatar = "";
    String realPlayerName = "";

    /**
     * Internal. Use {@link #of(String, String, String)}
     *
     * @param displayName The Username of the Author
     * @param avatar      The avatar URL of the Author
     * @param isServer    Is the Author the Minecraft Server
     */
    private DiscordAuthor(String displayName, String avatar, String username, boolean isServer, String uuid) {
        this.displayName = displayName;
        this.avatar = avatar;
        this.username = username;
        this.isServer = isServer;
        this.uuid = uuid;
    }

    /**
     * Create a new Discord Author
     *
     * @param displayName The name/Username of the Author
     * @param uuid        The Mojang UUID of the Author
     * @return A constructed {@link DiscordAuthor}
     */
    public static DiscordAuthor of(String displayName, String uuid, String username) {
        return new DiscordAuthor(
                displayName,
                SDLinkConfig.INSTANCE.chatConfig.playerAvatarType.resolve(SDLinkPlatform.minecraftHelper.isOnlineMode() ? uuid : username),
                username,
                false,
                SDLinkPlatform.minecraftHelper.isOnlineMode() ? uuid : username
        );
    }

    public static DiscordAuthor of(String displayName, String avatar, String username, boolean server) {
        return new DiscordAuthor(
                displayName,
                avatar,
                username,
                server,
                username
        );
    }

    public DiscordAuthor setPlayerAvatar(String usr, String userid) {
        realPlayerAvatar = SDLinkConfig.INSTANCE.chatConfig.playerAvatarType.resolve(SDLinkPlatform.minecraftHelper.isOnlineMode() ? userid : usr);
        return this;
    }

    public DiscordAuthor setPlayerName(String name) {
        this.realPlayerName = name;
        return this;
    }

    public DiscordAuthor setGameProfile(BridgedGameProfile profile) {
        this.profile = profile;
        this.username = profile.getName();
        this.uuid = profile.getId().toString();
        return this;
    }
}
