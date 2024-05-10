/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.general;

import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.services.SDLinkPlatform;
import com.hypherionmc.sdlink.core.util.SystemUtils;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * @author HypherionSA
 * Informational command to give you a quick overview of the hardware/player
 * status of your server
 */
public class ServerStatusSlashCommand extends SDLinkSlashCommand {

    public ServerStatusSlashCommand() {
        super(true);

        this.name = "status";
        this.help = "View information about your server";
        this.guildOnly = true;
    }

    public static MessageEmbed runStatusCommand() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        CentralProcessor cpu = hal.getProcessor();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Server Information / Status");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**__System Information__**\r\n\r\n");

        stringBuilder
                .append("**CPU:**\r\n```\r\n")
                .append(cpu.toString())
                .append("```")
                .append("\r\n");

        try {
            stringBuilder
                    .append("**Memory:**\r\n```\r\n")
                    .append(SystemUtils.byteToHuman(hal.getMemory().getAvailable()))
                    .append(" free of ")
                    .append(SystemUtils.byteToHuman(hal.getMemory().getTotal()))
                    .append("```\r\n");
        } catch (Exception ignored) {}

        stringBuilder
                .append("**OS:**\r\n```\r\n")
                .append(systemInfo.getOperatingSystem().toString())
                .append(" (")
                .append(systemInfo.getOperatingSystem().getBitness())
                .append(" bit)\r\n")
                .append("Version: ")
                .append(systemInfo.getOperatingSystem().getVersionInfo().getVersion())
                .append("```\r\n");

        stringBuilder
                .append("**System Uptime:**\r\n```\r\n")
                .append(SystemUtils.secondsToTimestamp(systemInfo.getOperatingSystem().getSystemUptime()))
                .append("```\r\n");

        stringBuilder.append("**__Minecraft Information__**\r\n\r\n");

        stringBuilder
                .append("**Server Uptime:**\r\n```\r\n")
                .append(SystemUtils.secondsToTimestamp(SDLinkPlatform.minecraftHelper.getServerUptime()))
                .append("```\r\n");

        stringBuilder
                .append("**Server Version:**\r\n```\r\n")
                .append(SDLinkPlatform.minecraftHelper.getServerVersion())
                .append("```\r\n");

        stringBuilder
                .append("**Players Online:**\r\n```\r\n")
                .append(SDLinkPlatform.minecraftHelper.getPlayerCounts().getLeft())
                .append("/")
                .append(SDLinkPlatform.minecraftHelper.getPlayerCounts().getRight())
                .append("```\r\n");

        stringBuilder
                .append("**Whitelisting:**\r\n```\r\n")
                .append(!SDLinkPlatform.minecraftHelper.checkWhitelisting().isError() ? "Enabled" : "Disabled")
                .append("```\r\n");

        builder.setDescription(stringBuilder.toString());

        return builder.build();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        Button refreshBtn = Button.danger("sdrefreshbtn", "Refresh");
        event.getHook().sendMessageEmbeds(runStatusCommand()).addActionRow(refreshBtn).queue();
    }
}
