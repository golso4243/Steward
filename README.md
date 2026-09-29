# Steward

Steward is a GUI-first staff and moderation suite for Fabric servers, made by SwornHero.

- Homepage: [stewardmod.com](https://www.stewardmod.com)
- Source: [github.com/golso4243/Steward](https://github.com/golso4243/Steward)
- Issues: [github.com/golso4243/Steward/issues](https://github.com/golso4243/Steward/issues)

## Requirements

- Minecraft `26.2`
- Java 25 or newer
- Fabric Loader `0.19.3` or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [LuckPerms](https://luckperms.net/) (required for permissions and staff hierarchy)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional)

## Features

Staff open the interface with `/steward`. Each module has its own documentation covering commands, permissions, and procedures:

- [Hierarchy and teleport](docs/Hierarchy-and-Teleport.md): rank-based action protection and staff teleport tools
- [Freeze](docs/Freeze-Module.md): hold a player in place during an investigation
- [Warnings](docs/Warning-Module.md): issue, track, acknowledge, and escalate warnings
- [Punishments](docs/Punishment-Module.md): issue and revoke punishments
- [Inventory inspection](docs/Inspection-Module.md): view player inventories
- [Vanish](docs/Vanish-Module.md): hide staff from players
- [Staff chat](docs/StaffChat-Module.md): private staff channel with history
- [Reports](docs/Report-Module.md): player reports with claim and resolve workflow
- [Staff notes](docs/StaffNotes-Module.md): persistent notes on players

## Building

```sh
git clone https://github.com/golso4243/Steward.git
cd Steward
./gradlew build
```

On Windows, use `gradlew.bat build`. The built jar is written to `build/libs/`.

To test locally, run `./gradlew runServer` or `./gradlew runClient`. For importing the project into an IDE, see the [Fabric getting-started guide](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up).

## License

Steward is licensed under the [Mozilla Public License 2.0](LICENSE).
