- REQUIRES [CraterLib](https://www.curseforge.com/minecraft/mc-mods/craterlib) - [Modrinth](https://modrinth.com/mod/craterlib)
- [Online Config Editor](https://editor.firstdark.dev)
- [Documentation](https://sdlink.fdd-docs.com)
- This single jar works on 1.18.2-1.21.4

*Requires CraterLib 2.1.3 or newer*

**Bug Fixes**:

- Fix bot randomly reconnecting to discord, and reloading everything and causing ratelimit spams
- Remove left over debug logging from LuckPerms rank syncing
- Fixed deathMessages config option being ignored - [#147](https://github.com/hypherionmc/sdlink/issues/147)
- Escape underscore (_) from usernames, so discord does not format it. - [#145](https://github.com/hypherionmc/sdlink/issues/145)

**New Features**:

- Add a new %current_time% placeholder for embeds, usable with `<t:TIME>` and similar - [#143](https://github.com/hypherionmc/sdlink/issues/143)