package com.hypherionmc.sdlinkrw.api.events

import com.hypherionmc.craterlib.core.event.CraterEvent
import com.hypherionmc.sdlinkrw.api.accounts.MinecraftAccount

class VerificationEvent {

    class PlayerVerified(val account: MinecraftAccount): CraterEvent()
    class PlayerUnverified(val account: MinecraftAccount): CraterEvent()
}