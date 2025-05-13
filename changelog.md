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
- Fix LinkedNames breaking in chat messages, when there is an unlinked account in the database - hypherionsa
- Fix Player Counts not excluding vanished players - hypherionsa

**New Features**:

- Add config toggle, to silence invalid/disallowed command feedback messages for linked commands - hypherionsa
- Add `%role_color%` placeholder for embeds, to use the user color of linked accounts - hypherionsa
- Integration with Advanced Chat, to prevent private chats from being relayed to discord - hypherionsa
- Added better replied to message indications in game - hypherionsa
- Add support for FTB Teams `chat` command - hypherionsa
- Allow mentioning users, channels and roles from chat without client side install - hypherionsa
- Config Editor command now sends link to user that executed the command - hypherionsa
- Implement basic support for custom emotes sent in Minecraft Chat - hypherionsa

**Changes**:

- Swap to own Avatar service, due to ongoing issues with 3rd party services - hypherionsa