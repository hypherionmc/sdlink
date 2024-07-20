/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import net.dv8tion.jda.api.entities.Activity;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * Config Structure for the Core bot settings
 */
public class BotConfigSettings {

    @Path("botToken")
    @SpecComment("The token of the Discord Bot to use. This will be encrypted on first load. See https://sdlink.fdd-docs.com/initial-setup/ to find this")
    public String botToken = "";

    @Path("silentReplies")
    @SpecComment("Use silent replies when Slash Commands are used")
    public boolean silentReplies = true;

    @Path("statusUpdateInterval")
    @SpecComment("How often the Bot Status will update on Discord (in Seconds). Set to 0 to disable")
    public int statusUpdateInterval = 30;

    @Path("botStatus")
    @SpecComment("Control what the Discord Bot will display as it's status message")
    public BotStatus botStatus = new BotStatus();

    @Path("topicUpdates")
    @SpecComment("Define how the bot should handle channel topic updates on the chat channel")
    public ChannelTopic channelTopic = new ChannelTopic();

    @Path("invite")
    @SpecComment("Configure the in-game Discord Invite command")
    public DiscordInvite invite = new DiscordInvite();

    public static class BotStatus {
        @Path("status")
        @SpecComment("Do not add Playing. A status to display on the bot. You can use %players% and %maxplayers% to show the number of players on the server")
        public String botStatus = "Enjoying Minecraft with %players%/%maxplayers% players";

        @Path("botStatusType")
        @SpecComment("The type of the status displayed on the bot. Valid entries are: PLAYING, STREAMING, WATCHING, LISTENING, CUSTOM_STATUS")
        public Activity.ActivityType botStatusType = Activity.ActivityType.CUSTOM_STATUS;

        @Path("botStatusStreamingURL")
        @SpecComment("The URL that will be used when the \"botStatusType\" is set to \"STREAMING\", required to display as \"streaming\".")
        public String botStatusStreamingURL = "https://twitch.tv/twitch";
    }

    public static class ChannelTopic {
        @Path("doTopicUpdates")
        @SpecComment("Should the bot update the topic of your chat channel automatically every 6 Minutes")
        public boolean doTopicUpdates = true;

        @Path("updateInterval")
        @SpecComment("How often should the bot update the channel topic (IN MINUTES)? CANNOT BE LOWER THAN 6 MINUTES!")
        public int updateInterval = 6;

        @Path("channelTopic")
        @SpecComment("A topic for the Chat Relay channel. You can use %player%, %maxplayers%, %uptime% or just leave it empty.")
        public String channelTopic = "Playing Minecraft with %players%/%maxplayers% people | Uptime: %uptime%";
    }

    public static class DiscordInvite {
        @Path("inviteLink")
        @SpecComment("If this is defined, it will enable the in-game Discord command")
        public String inviteLink = "";

        @Path("inviteMessage")
        @SpecComment("The message to show when someone uses /discord command. You can use %inviteurl%")
        public String inviteMessage = "Hey, check out our discord server here -> %inviteurl%";
    }

}
