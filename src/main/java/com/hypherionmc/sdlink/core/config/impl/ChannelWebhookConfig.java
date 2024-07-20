/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;


import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * Config Structure to control Channels and Webhooks used by the bot
 */
public class ChannelWebhookConfig {

    @Path("serverAvatar")
    @SpecComment("A DIRECT link to an image to use as the avatar for server messages. Also used for embeds")
    public String serverAvatar = "";

    @Path("serverName")
    @SpecComment("The name to display for Server messages when using Webhooks")
    public String serverName = "Minecraft Server";

    @Path("channels")
    @SpecComment("Config relating to the discord channels to use with the mod")
    public Channels channels = new Channels();

    @Path("webhooks")
    @SpecComment("Config relating to the discord Webhooks to use with the mod")
    public Webhooks webhooks = new Webhooks();

    public static class Channels {
        @Path("chatChannelID")
        @SpecComment("REQUIRED! The ID of the channel to post in and relay messages from. This is still needed, even in webhook mode")
        public String chatChannelID = "0";

        @Path("eventsChannelID")
        @SpecComment("If this ID is set, event messages will be posted in this channel instead of the chat channel")
        public String eventsChannelID = "0";

        @Path("consoleChannelID")
        @SpecComment("If this ID is set, console messages sent after the bot started will be relayed here")
        public String consoleChannelID = "0";
    }

    public static class Webhooks {
        @Path("enabled")
        @SpecComment("Prefer Webhook Messages over Standard Bot Messages")
        public boolean enabled = false;

        @Path("webhookNameFormat")
        @SpecComment("Change how the webhook name is displayed in discord. Available placeholders: %display_name%, %mc_name%")
        public String webhookNameFormat = "%display_name%";

        @Path("chatWebhook")
        @SpecComment("The URL of the channel webhook to use for Chat Messages. Will be encrypted on first run")
        public String chatWebhook = "";

        @Path("eventsWebhook")
        @SpecComment("The URL of the channel webhook to use for Server Messages. Will be encrypted on first run")
        public String eventsWebhook = "";

        @Path("consoleWebhook")
        @SpecComment("The URL of the channel webhook to use for Console Messages. DOES NOT WORK FOR CONSOLE RELAY! Will be encrypted on first run")
        public String consoleWebhook = "";
    }

}
