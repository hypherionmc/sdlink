package com.hypherionmc.sdlinkrw.api.events

import com.hypherionmc.craterlib.core.event.CraterEvent
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount

/**
 * @author HypherionSA
 *
 * Events that get fired when a player is verified or unverified.
 */
class VerificationEvent {

    /**
     * Fired when a player is verified.
     */
    class PlayerVerified(val account: MinecraftAccount): CraterEvent()

    /**
     * Fired when a player is unverified.
     */
    class PlayerUnverified(val account: MinecraftAccount): CraterEvent()
}