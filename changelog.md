- REQUIRES [CraterLib](https://www.curseforge.com/minecraft/mc-mods/craterlib) - [Modrinth](https://modrinth.com/mod/craterlib)
- [Online Config Editor](https://editor.firstdark.dev)
- [Documentation](https://sdlink.fdd-docs.com)
- This single jar works on 1.18.2-1.21.4

*Requires CraterLib 2.1.3 or newer*

**Bug Fixes**:

- Fix /discordverify command permissions, so that it's usable by everyone - hypherionsa
- Fix Database engine failing to read database files that end with 2 empty lines - hypherionsa
- Don't relay raw Config Editor URLs to Console Relay - hypherionsa
- Fix `%player_name%` placeholder not being usable in Chat Embeds - hypherionsa
- Fix certain methods of bypassing the mention filter - unsigned-32-bit-integer - [#151](https://github.com/hypherionmc/sdlink/pull/151)

**New Features**:

- Added Vanish Config flag to disable fake join/leave messages - hypherionsa
- Allow Regex matching for Message filtering - hypherionsa
- Allow message filtering to be used on Usernames as well - hypherionsa
- Access Control/Optional Verification can now give multiple `verifiedRole`s - hypherionsa
- Allow advancements and death messages to respect game rules - hypherionsa
- Hard coded english messages and responses can now be translated into different languages (TRANSLATORS WANTED) - hypherionsa