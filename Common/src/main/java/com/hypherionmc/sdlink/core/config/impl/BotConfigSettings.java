/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.craterlib.libs.moonconfig.core.fields.RandomArrayList;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HypherionSA
 * Config Structure for the Core bot settings
 */
public final class BotConfigSettings {

    @Path("botToken")
    @SpecComment("The token of the Discord Bot to use. This will be encrypted on first load. See https://sdlink.fdd-docs.com/installation/bot-creation/ to find this")
    public String botToken = "";

    @Path("serverAvatar")
    @SpecComment("A DIRECT link to an image to use as the avatar for server messages. Only used with webhooks and embeds")
    public String serverAvatar = "";

    @Path("serverName")
    @SpecComment("The name to use when the Server is the author a message.")
    public String serverName = "Minecraft Server";

    @Path("printInviteLink")
    @SpecComment("Print the bot invite link to the console on startup")
    public boolean printInviteLink = true;

    @Path("silentReplies")
    @SpecComment("Use silent replies when Slash Commands are used")
    public boolean silentReplies = true;

    @Path("statusUpdateInterval")
    @SpecComment("How often the Bot Status will update on Discord (in Seconds). Set to 0 to disable")
    public int statusUpdateInterval = 30;

    @Path("defaultChannels")
    @SpecComment("Global Channels that can be used throughout the config. If you just use a single discord server, this is all you need")
    public DefaultChannels defaultChannels = new DefaultChannels();

    @Path("botStatus")
    @SpecComment("Control what the Discord Bot will display as it's status message")
    public RandomArrayList<BotStatus> botStatus = RandomArrayList.of(new BotStatus());

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

    public static class DefaultChannels {

        @Path("default_chat")
        @SpecComment("List of IDs to use for `default_chat` channels. Can be from the same discord, or different discord servers")
        public List<String> default_chat =  new ArrayList<>();

        @Path("default_event")
        @SpecComment("List of IDs to use for `default_event` channels. Can be from the same discord, or different discord servers")
        public List<String> default_event =  new ArrayList<>();

        @Path("default_console")
        @SpecComment("List of IDs to use for `default_console` channels. Can be from the same discord, or different discord servers")
        public List<String> default_console =  new ArrayList<>();

    }

}
