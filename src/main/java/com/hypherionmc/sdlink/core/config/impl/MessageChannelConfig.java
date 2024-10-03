/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import com.hypherionmc.sdlink.core.messaging.MessageDestination;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.util.DestinationHolder;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * Config Structure to control the destinations of messages
 */
public class MessageChannelConfig {

    @Path("chat")
    @SpecComment("Control where CHAT messages are delivered")
    public DestinationObject chat = DestinationObject.of(MessageDestination.CHAT, false, "default");

    @Path("start")
    @SpecComment("Control where START messages are delivered")
    public DestinationObject start = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("stop")
    @SpecComment("Control where STOP messages are delivered")
    public DestinationObject stop = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("join")
    @SpecComment("Control where JOIN messages are delivered")
    public DestinationObject join = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("leave")
    @SpecComment("Control where LEAVE messages are delivered")
    public DestinationObject leave = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("advancements")
    @SpecComment("Control where ADVANCEMENT messages are delivered")
    public DestinationObject advancements = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("death")
    @SpecComment("Control where DEATH messages are delivered")
    public DestinationObject death = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("commands")
    @SpecComment("Control where COMMAND messages are delivered")
    public DestinationObject commands = DestinationObject.of(MessageDestination.EVENT, false, "default");

    @Path("custom")
    @SpecComment("Control where messages that match none of the above are delivered")
    public DestinationObject custom = DestinationObject.of(MessageDestination.EVENT, false, "default");

    public static class DestinationObject {
        @Path("channel")
        @SpecComment("The Channel the message will be delivered to. Valid entries are CHAT, EVENT, CONSOLE, OVERRIDE")
        public MessageDestination channel;

        @Path("useEmbed")
        @SpecComment("Should the message be sent using EMBED style messages")
        public boolean useEmbed;

        @Path("embedLayout")
        @SpecComment("Embed Layout to use")
        public String embedLayout;

        @Path("override")
        @SpecComment("Override the destination with a custom channel/webhook url. Make sure to change `channel` above to OVERRIDE")
        public String override;

        DestinationObject(MessageDestination destination, boolean useEmbed, String embedLayout, String override) {
            this.channel = destination;
            this.useEmbed = useEmbed;
            this.embedLayout = embedLayout;
            this.override = override;
        }

        public static DestinationObject of(MessageDestination destination, boolean useEmbed, String embedLayout) {
            return new DestinationObject(destination, useEmbed, embedLayout, "");
        }

        public DestinationHolder toHolder(MessageType type) {
            return DestinationHolder.of(this, type);
        }
    }
}
