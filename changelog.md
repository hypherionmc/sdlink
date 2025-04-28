- REQUIRES [CraterLib](https://www.curseforge.com/minecraft/mc-mods/craterlib) - [Modrinth](https://modrinth.com/mod/craterlib)
- [Online Config Editor](https://editor.firstdark.dev)
- [Documentation](https://sdlink.fdd-docs.com)
- This single jar works on 1.18.2-1.21.5

*Requires CraterLib 2.1.5 or newer*

**Bug Fixes**:

- Fix Translation system being able to grab the wrong default files from resources - hypherionsa
- Possible bug fix for shutdown hang when an error occurs on shutdown - hypherionsa
- Added temporary code to forcefully regenerate language files that are invalid, due to file loading bug - hypherionsa
- Fixed channel IDs being set to "" causing a startup failure - hypherionsa
- Fix config migrator crash when migrating configs older than version 21 (3.2.0 and older) - hypherionsa

**New Features**:

- Add config toggle, to silence invalid/disallowed command feedback messages for linked commands - hypherionsa
- Add `%role_color%` placeholder for embeds, to use the user color of linked accounts - hypherionsa
- Integration with Advanced Chat, to prevent private chats from being relayed to discord - hypherionsa
- Added better replied to message indications in game - hypherionsa

**Changes**:

- Swap to own Avatar service, due to ongoing issues with 3rd party services - hypherionsa