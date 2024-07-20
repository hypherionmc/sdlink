/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.general;

import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.commands.CommandManager;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Set;

/**
 * @author HypherionSA
 * The Help Command for the bot
 */
public class HelpSlashCommand extends SDLinkSlashCommand {

    public HelpSlashCommand() {
        super(false);
        this.name = "help";
        this.help = "Bot commands and help";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        Set<SlashCommand> commands = CommandManager.INSTANCE.getCommands();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot commands");
        builder.setColor(Color.BLUE);

        commands.forEach(cmd -> builder.addField(cmd.getName(), cmd.getHelp(), false));
        event.getHook().sendMessageEmbeds(builder.build()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
    }
}
