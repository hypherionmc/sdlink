package com.hypherionmc.sdlink.core.config.impl.compat;

import net.dv8tion.jda.api.OnlineStatus;
import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

public class MaintenanceModeCompat {

    @Path("enabled")
    @SpecComment("Should integration with MaintenanceMode be enabled")
    public boolean enabled = true;

    @Path("maintenanceOnlineStatus")
    @SpecComment("Change the Bot Online Status during Maintenance Mode. Valid options are ONLINE, IDLE, DO_NOT_DISTURB, OFFLINE")
    public OnlineStatus onlineStatus = OnlineStatus.DO_NOT_DISTURB;

    @Path("updateChannelTopic")
    @SpecComment("Update channel topic with server MOTD during maintenance mode")
    public boolean updateChannelTopic = true;

    @Path("updateBotStatus")
    @SpecComment("Update the bot status with the server MOTD during maintenance mode")
    public boolean updateBotStatus = false;

    @Path("sendMaintenanceStart")
    @SpecComment("Send a message to discord when maintenance starts")
    public boolean sendMaintenanceStart = true;

    @Path("sendMaintenanceEnd")
    @SpecComment("Send a message to discord when maintenance ends")
    public boolean sendMaintenanceEnd = true;

    @Path("maintenanceStartMessage")
    @SpecComment("The message to send to discord when maintenance has started")
    public String maintenanceStartMessage = "Maintenance has started";

    @Path("maintenanceEndMessage")
    @SpecComment("The message to send to discord when maintenance has ended")
    public String maintenanceEndMessage = "Maintenance has ended";

}
