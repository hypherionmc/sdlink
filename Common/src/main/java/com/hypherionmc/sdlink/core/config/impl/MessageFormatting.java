/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;


import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * Config Structure to control Discord/MC Message Formatting
 */
public final class MessageFormatting {

    @Path("mcPrefix")
    @SpecComment("Prefix to add to Minecraft when a message is relayed from Discord. Supports MiniMessage formatting. Use %user% for the Discord Username")
    public String mcPrefix = "<yellow>[Discord]<reset> %user%: ";

    @Path("mcReplyFormatting")
    @SpecComment("How messages relayed from discord that are replies to other messages are formatted. Supports MiniMessage formatting")
    public String mcReplyFormatting = "    <b>┌────<reset> %color%@%replier_name%%end_color% <gray>%message_summary%<newline><reset>";

    @Path("serverStarting")
    @SpecComment("Server Starting Message")
    public String serverStarting = "*Server is starting...*";

    @Path("serverStartingColor")
    @SpecComment("Optional embed color for the Server Starting Message. Leave empty to use the embed JSON color")
    public String serverStartingColor = "";

    @Path("serverStarted")
    @SpecComment("Server Started Message")
    public String serverStarted = "*Server has started. Enjoy!*";

    @Path("serverStartedColor")
    @SpecComment("Optional embed color for the Server Started Message. Leave empty to use the embed JSON color")
    public String serverStartedColor = "";

    @Path("serverStopping")
    @SpecComment("Server Stopping Message")
    public String serverStopping = "*Server is stopping...*";

    @Path("serverStoppingColor")
    @SpecComment("Optional embed color for the Server Stopping Message. Leave empty to use the embed JSON color")
    public String serverStoppingColor = "";

    @Path("serverStopped")
    @SpecComment("Server Stopped Message")
    public String serverStopped = "*Server has stopped...*";

    @Path("serverStoppedColor")
    @SpecComment("Optional embed color for the Server Stopped Message. Leave empty to use the embed JSON color")
    public String serverStoppedColor = "";

    @Path("playerJoined")
    @SpecComment("Player Joined Message. Use %player% to display the player name")
    public String playerJoined = "*%player% has joined the server!*";

    @Path("playerJoinedColor")
    @SpecComment("Optional embed color for Player Joined Messages. Leave empty to use the embed JSON color")
    public String playerJoinedColor = "";

    @Path("playerLeft")
    @SpecComment("Player Left Message. Use %player% to display the player name")
    public String playerLeft = "*%player% has left the server!*";

    @Path("playerLeftColor")
    @SpecComment("Optional embed color for Player Left Messages. Leave empty to use the embed JSON color")
    public String playerLeftColor = "";

    @Path("advancements")
    @SpecComment("Advancement Messages. Available variables: %player%, %title%, %description%")
    public String achievements = "*%player% has made the advancement [%title%]: %description%*";

    @Path("advancementsColor")
    @SpecComment("Optional embed color for Advancement Messages. Leave empty to use the embed JSON color")
    public String achievementsColor = "";

    @Path("chat")
    @SpecComment("Chat Messages. THIS DOES NOT APPLY TO EMBED OR WEBHOOK MESSAGES. Available variables: %player%, %message%, %mcname%")
    public String chat = "%player%: %message%";

    @Path("chatColor")
    @SpecComment("Optional embed color for Chat Messages. Leave empty to use the embed JSON color")
    public String chatColor = "";

    @Path("death")
    @SpecComment("Death Messages. Available variables: %player%, %message%")
    public String death = "%player% %message%";

    @Path("deathColor")
    @SpecComment("Optional embed color for Death Messages. Leave empty to use the embed JSON color")
    public String deathColor = "";

    @Path("whitelistAdded")
    @SpecComment("Message to be sent when a player is added to the whitelist")
    public String whitelistAdded = "%player% has been whitelisted!";

    @Path("whitelistAddedColor")
    @SpecComment("Optional embed color for Whitelist Added Messages. Leave empty to use the embed JSON color")
    public String whitelistAddedColor = "";

    @Path("whitelistRemoved")
    @SpecComment("Message to be sent when a player is removed from the whitelist")
    public String whitelistRemoved = "%player% has been removed from the whitelist!";

    @Path("whitelistRemovedColor")
    @SpecComment("Optional embed color for Whitelist Removed Messages. Leave empty to use the embed JSON color")
    public String whitelistRemovedColor = "";

    @Path("commands")
    @SpecComment("Command Messages. Available variables: %player%, %command%")
    public String commands = "%player% **executed command**: *%command%*";

    @Path("commandsColor")
    @SpecComment("Optional embed color for Command Messages. Leave empty to use the embed JSON color")
    public String commandsColor = "";
}
