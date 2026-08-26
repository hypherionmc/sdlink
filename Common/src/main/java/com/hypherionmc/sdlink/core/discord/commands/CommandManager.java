/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.sdlink.core.discord.commands.slash.general.HelpSlashCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.general.PlayerListSlashCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.general.ServerStatusSlashCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.general.SyncCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.hide.HiddenPlayersCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.hide.HidePlayerCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.hide.UnhidePlayerCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.setup.ReloadCacheCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.setup.SetChannelCommand;
import com.hypherionmc.sdlink.core.discord.commands.slash.verification.*;
import com.hypherionmc.sdlinkrw.api.events.SlashCommandRegistrationEvent;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.SlashCommand;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author HypherionSA
 * Command Manager class to control how commands are registered to discord
 */
@Getter
public final class CommandManager {

    public static final CommandManager INSTANCE = new CommandManager();

    private final Set<SlashCommand> commands = new HashSet<>();

    private CommandManager() {
        this.addCommands();
    }

    private void addCommands() {
        commands.clear();

        // Access Control Commands
        commands.add(new VerifyAccountCommand());
        commands.add(new UnverifyAccountSlashCommand());
        commands.add(new StaffUnverifyCommand());
        commands.add(new StaffVerifyAccountCommand());
        commands.add(new ViewVerifiedAccounts());
        commands.add(new SyncCommand());

        // Enable the Server Status command
        commands.add(new ServerStatusSlashCommand());

        // Enable the Player List command
        commands.add(new PlayerListSlashCommand());

        // Enable the Help command
        commands.add(new HelpSlashCommand());

        // SetChannel config Command
        commands.add(new SetChannelCommand());

        // Reload Cache command
        commands.add(new ReloadCacheCommand());

        // Hidden Players
        commands.add(new HidePlayerCommand());
        commands.add(new HiddenPlayersCommand());
        commands.add(new UnhidePlayerCommand());
    }

    /**
     * INTERNAL. Used to register slash commands
     *
     * @param client The Discord Command Client instance
     */
    public void register(CommandClient client) {
        SlashCommandRegistrationEvent event = new SlashCommandRegistrationEvent();
        CraterEventBus.INSTANCE.postEvent(event);

        commands.addAll(event.getCommands());
        commands.forEach(client::addSlashCommand);
    }

}
