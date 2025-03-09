/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import com.hypherionmc.sdlink.core.config.AvatarType;
import com.hypherionmc.sdlink.core.config.TriBoolean;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HypherionSA
 * Config Structure to control what types of messages are supported by the mod
 */
public final class ChatSettingsConfig {

    @Path("useLinkedNames")
    @SpecComment("Use linked account names in Discord/Minecraft messages, instead of the default ones")
    public boolean useLinkedNames = true;

    @Path("showDiscordInfo")
    @SpecComment("Show the discord name, username and role of the user that sent a message in Minecraft when the message is hovered")
    public boolean showDiscordInfo = false;

    @Path("formatting")
    @SpecComment("Convert Discord to MC, and MC to Discord Formatting")
    public boolean formatting = true;

    @Path("sendConsoleMessages")
    @SpecComment("Should console messages be sent to the Console Channel")
    public boolean sendConsoleMessages = false;

    @Path("customAvatarService")
    @SpecComment("Add your own Avatar service URL here. Use {uuid} to replace the player ID in the URL")
    public String customAvatarService = "https://crafatar.com/avatars/{uuid}";

    @Path("playerAvatarType")
    @SpecComment("The type of image to use as the player icon in messages. Valid entries are: AVATAR, HEAD, BODY, COMBO, CUSTOM")
    public AvatarType playerAvatarType = AvatarType.HEAD;

    @Path("relayTellRaw")
    @SpecComment("Should messages sent with TellRaw be sent to discord as a chat? (Experimental)")
    public boolean relayTellRaw = true;

    @Path("relayFullCommands")
    @SpecComment("Should the entire command executed be relayed to discord, or only the name of the command")
    public boolean relayFullCommands = false;

    @Path("ignoreBots")
    @SpecComment("Should messages from bots be relayed")
    public boolean ignoreBots = true;

    @Path("serverStarting")
    @SpecComment("Should SERVER STARTING messages be shown")
    public boolean serverStarting = true;

    @Path("serverStarted")
    @SpecComment("Should SERVER STARTED messages be shown")
    public boolean serverStarted = true;

    @Path("serverStopping")
    @SpecComment("Should SERVER STOPPING messages be shown")
    public boolean serverStopping = true;

    @Path("serverStopped")
    @SpecComment("Should SERVER STOPPED messages be shown")
    public boolean serverStopped = true;

    @Path("playerMessages")
    @SpecComment("Should the chat be relayed")
    public boolean playerMessages = true;

    @Path("discordMessages")
    @SpecComment("Should discord messages be relayed to Minecraft")
    public boolean discordMessages = true;

    @Path("playerJoin")
    @SpecComment("Should Player Join messages be posted")
    public boolean playerJoin = true;

    @Path("playerLeave")
    @SpecComment("Should Player Leave messages be posted")
    public boolean playerLeave = true;

    @Path("advancementMessages")
    @SpecComment("Should Advancement messages be posted. Valid values are ALWAYS, NEVER or GAMERULE")
    public TriBoolean advancementMessages = TriBoolean.ALWAYS;

    @Path("deathMessages")
    @SpecComment("Should Death Announcements be posted. Valid values are ALWAYS, NEVER or GAMERULE")
    public TriBoolean deathMessages = TriBoolean.ALWAYS;

    @Path("sendSayCommand")
    @SpecComment("Should Messages from the /say command be posted")
    public boolean sendSayCommand = true;

    @Path("broadcastCommands")
    @SpecComment("Should commands be posted to discord")
    public boolean broadcastCommands = true;

    @Path("whitelistChanged")
    @SpecComment("Should whitelist changes be posted to discord")
    public boolean whitelistChanged = false;

    @Path("ignoredCommands")
    @SpecComment("Commands that should not be broadcast to discord")
    public List<String> ignoredCommands = new ArrayList<>() {{
        add("particle");
        add("login");
        add("execute");
        add("sdconfigeditor");
    }};

    @Path("allowMentionsFromChat")
    @SpecComment("Allow mentioning discord roles, users and channels from Minecraft Chat")
    public boolean allowMentionsFromChat = false;
}
