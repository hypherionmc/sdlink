package com.hypherionmc.sdlink.core.config.impl;

import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.Path;
import com.hypherionmc.craterlib.libs.moonconfig.core.conversion.SpecComment;

public final class RelayMessageConfig {

    @Path("relayMinecraftChats")
    @SpecComment("Should Minecraft Chats be relayed to all connected servers")
    public boolean relayMinecraftChats = true;

    @Path("relayDiscordChats")
    @SpecComment("Should Discord Chat Messages be relayed to all connected servers")
    public boolean relayDiscordChats = true;

    @Path("relayMinecraftToDiscord")
    @SpecComment("Should Minecraft Messages be relayed to connected discord servers")
    public boolean relayMinecraftToDiscord = true;

    @Path("relayDeathMessages")
    @SpecComment("Should player death messages be relayed to all connected servers")
    public boolean relayDeathMessages = true;

    @Path("relayAdvancementMessages")
    @SpecComment("Should player advancement messages be relayed to all connected servers")
    public boolean relayAdvancementMessages = true;

    @Path("relayJoinMessages")
    @SpecComment("Should player join messages be relayed to all connected servers")
    public boolean relayJoinMessages = true;

    @Path("relayLeaveMessages")
    @SpecComment("Should player leave messages be relayed to all connected servers")
    public boolean relayLeaveMessages = true;

    @Path("relayMessagePrefix")
    @SpecComment("The Prefix to use for messages relayed to other servers. Set this to empty to ignore it")
    public String relayMessagePrefix = "<blue>[%server_name%]</blue>";
}
