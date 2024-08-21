/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;


import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * Config Structure to control Discord/MC Message Formatting
 */
public class MessageFormatting {

    @Path("mcPrefix")
    @SpecComment("Prefix to add to Minecraft when a message is relayed from Discord. Supports MC formatting. Use %user% for the Discord Username")
    public String mcPrefix = "\u00A7e[Discord]\u00A7r %user%: ";

    @Path("serverStarting")
    @SpecComment("Server Starting Message")
    public String serverStarting = "*Server is starting...*";

    @Path("serverStarted")
    @SpecComment("Server Started Message")
    public String serverStarted = "*Server has started. Enjoy!*";

    @Path("serverStopping")
    @SpecComment("Server Stopping Message")
    public String serverStopping = "*Server is stopping...*";

    @Path("serverStopped")
    @SpecComment("Server Stopped Message")
    public String serverStopped = "*Server has stopped...*";

    @Path("playerJoined")
    @SpecComment("Player Joined Message. Use %player% to display the player name")
    public String playerJoined = "*%player% has joined the server!*";

    @Path("playerLeft")
    @SpecComment("Player Left Message. Use %player% to display the player name")
    public String playerLeft = "*%player% has left the server!*";

    @Path("achievements")
    @SpecComment("Achievement Messages. Available variables: %player%, %title%, %description%")
    public String achievements = "*%player% has made the advancement [%title%]: %description%*";

    @Path("chat")
    @SpecComment("Chat Messages. THIS DOES NOT APPLY TO EMBED OR WEBHOOK MESSAGES. Available variables: %player%, %message%, %mcname%")
    public String chat = "%player%: %message%";

    @Path("death")
    @SpecComment("Death Messages. Available variables: %player%, %message%")
    public String death = "%player% %message%";

    @Path("commands")
    @SpecComment("Command Messages. Available variables: %player%, %command%")
    public String commands = "%player% **executed command**: *%command%*";
}
