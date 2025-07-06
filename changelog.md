- REQUIRES [CraterLib](https://www.curseforge.com/minecraft/mc-mods/craterlib) - [Modrinth](https://modrinth.com/mod/craterlib)
- [Online Config Editor](https://editor.firstdark.dev)
- [Documentation](https://sdlink.fdd-docs.com)
- This single jar works on 1.18.2-1.21.7

*Requires CraterLib 2.1.5 or newer*

**Bug Fixes**:

- Add Missing Player Data to Server Embeds (Embeds that are usually sent as the server) - hypherionsa
- Fix mcPrefix with old mc formatting (Player names) in Minecraft Chat not showing - hypherionsa
- Fix Events Channel messages going to chat channel, even when event channel is defined - hypherionsa
- Fix Linked Names in chat formatting showing up as Unknown - hypherionsa
- Fix Username filters not applying - hypherionsa
- Fix Console relay causing an error on server shutdown - hypherionsa
- Fix profile data persisting on Server messages - hypherionsa
- Fix Linked Names feature using incorrect UUID for players - hypherionsa
- Fix CustomEmotes not loading and CustomEmotes not being usable when mentions are disabled - hypherionsa

**New Features**:

- Add config option to use the server author for Chat Webhook messages (because it was requested) - hypherionsa
- Add new `CONSOLE` target option for filter entries - ArkoSammy12
- Add %mcname% placeholder for other message types going to discord - hypherionsa