package com.hypherionmc.sdlink.core.config.impl.channels;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;
import com.hypherionmc.sdlink.api.messaging.MessageType;

public class ChannelConfig {

    @Path("chat")
    @SpecComment("Chat Messages")
    public ChatMessagesConfig chatMessages = new ChatMessagesConfig();

    @Path("start")
    @SpecComment("Server Starting Messages")
    public GenericMessageConfig startMessages = new GenericMessageConfig(MessageType.START, "*Server is starting...*");

    @Path("started")
    @SpecComment("Server Started Messages")
    public GenericMessageConfig startedMessages = new GenericMessageConfig(MessageType.STARTED, "*Server is starting...*");

    @Path("stop")
    @SpecComment("Server Stopping Messages")
    public GenericMessageConfig stopMessages = new GenericMessageConfig(MessageType.STOP, "*Server is stopping...*");

    @Path("stopped")
    @SpecComment("Server Stopped Messages")
    public GenericMessageConfig stoppedMessages = new GenericMessageConfig(MessageType.STOPPED, "*Server has stopped...*");

    @Path("join")
    @SpecComment("Player Join Messages")
    public GenericMessageConfig joinMessages = new GenericMessageConfig(MessageType.JOIN, "*%player% has joined the server!*");

    @Path("leave")
    @SpecComment("Player Leave Messages")
    public GenericMessageConfig leaveMessages = new GenericMessageConfig(MessageType.LEAVE, "*%player% has left the server!*");

    @Path("advancements")
    @SpecComment("Player Advancement Messages")
    public GameRuleMessageConfig advancementMessages = new GameRuleMessageConfig(MessageType.ADVANCEMENTS, "*%player% has made the advancement [%title%]: %description%*");

    @Path("death")
    @SpecComment("Player Death Messages")
    public GameRuleMessageConfig deathMessages = new GameRuleMessageConfig(MessageType.DEATH, "%player% %message%");

    @Path("commands")
    @SpecComment("Command Messages")
    public CommandMessageConfig commandMessages = new CommandMessageConfig();

    @Path("whitelist_add")
    @SpecComment("Player Added to the whitelist")
    public GenericMessageConfig whitelistAddMessages = new GenericMessageConfig(MessageType.WHITELIST, "%player% has been whitelisted!");

    @Path("whitelist_remove")
    @SpecComment("Player Added to the whitelist")
    public GenericMessageConfig whitelistRemoveMessages = new GenericMessageConfig(MessageType.WHITELIST_REMOVE, "%player% has been removed from the whitelist!");

    @Path("custom")
    @SpecComment("Control where messages that match none of the above are delivered")
    public GenericMessageConfig customMessages = new GenericMessageConfig(MessageType.CUSTOM, "%message%");

    @Path("console")
    @SpecComment("Console Relay Messages")
    public ConsoleMessageConfig consoleMessages = new ConsoleMessageConfig();

}
