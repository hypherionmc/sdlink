/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.accounts;

import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.Nullable;

/**
 * @author HypherionSA
 * Represents a Message Author for messages sent from Minecraft to Discord
 */
@Getter
public final class DiscordAuthor {

    private String displayName;
    private String avatar;
    private final boolean isServer;
    private String username;
    private String uuid;
    private CraterGameProfile profile;
    String realPlayerAvatar = "";
    String realPlayerName = "";
    @Setter private int color = Role.DEFAULT_COLOR_RAW;

    /**
     * Internal. Use {@link #of(String, String, String)}
     *
     * @param displayName The Username of the Author
     * @param avatar      The avatar URL of the Author
     * @param isServer    Is the Author the Minecraft Server
     */
    private DiscordAuthor(String displayName, String avatar, String username, boolean isServer, String uuid) {
        //this.displayName = displayName.replace("_", "\\_");
        this.avatar = avatar;
        this.username = username;
        this.isServer = isServer;
        this.uuid = uuid;
        this.displayName = displayName;
        this.profile = null;

        fixDisplayName(displayName);
    }

    private void fixDisplayName(String displayName) {
        this.displayName = this.displayName.replace("_", "\\_");
        this.displayName = SDLinkChatUtils.applyFiltering(
                this.displayName,
                (i) -> i.appliesTo.appliesToUsername(i));

        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = displayName;
            this.displayName = this.displayName.replace("_", "\\_");
        }
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
                uuid
        );
    }

    public static DiscordAuthor getServer() {
        return new DiscordAuthor(
                SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName,
                SDLinkConfig.INSTANCE.channelsAndWebhooks.serverAvatar,
                "server",
                true,
                ""
        ).setPlayerName(SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName);
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

    public void overrideData(String name, String avatar) {
        this.displayName = name;
        this.avatar = avatar;
        this.realPlayerAvatar = avatar;
        fixDisplayName(name);
    }

    public void overrideData(String name) {
        this.displayName = name;
        fixDisplayName(name);
    }

    public DiscordAuthor setGameProfile(@Nullable CraterGameProfile profile) {
        this.profile = profile;
        if (profile != null) {
            this.username = profile.getName();
            this.uuid = profile.getId().toString();
        }
        return this;
    }
}
