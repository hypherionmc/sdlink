package com.hypherionmc.sdlinkrw.api.events

import com.hypherionmc.craterlib.core.event.CraterEvent
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand

/**
 * @author HypherionSA
 *
 * Event that is fired before Simple Discord Link registers its slash commands
 */
class SlashCommandRegistrationEvent(): CraterEvent() {

    val commands: MutableList<SDLinkSlashCommand> = mutableListOf()

    /**
     * Add your own slash command
     *
     * @param command A copy of your command class extending {@link SDLinkSlashCommand}
     */
    fun addCommand(command: SDLinkSlashCommand) {
        commands.add(command)
    }
}