# Sage

Sage is a simple replacement for plugins like CMI & Essentials. It includes the core commands and a staffing system.

## Installation

Download the latest release from the github page.
Add it to your plugins folder and reload your server.

## Permissions

Use a permission handler like Luckperms to handle permissions for every command.

## Commands & Permissions Quick Reference

| Command | Permission | Default | Description |
|---------|-----------|---------|-------------|
| `/sit` | `sage.sit` | true | Sit down on the ground or on blocks |
| `/lay` | `sage.lay` | true | Lay down on the ground |
| `/spin` | `sage.spin` | true | Spin around in a circle |
| `/crawl` | `sage.crawl` | true | Crawl on the ground like a sneaking player |
| `/hat` | `sage.hat` | OP | Place item in hand on your head as a hat |
| `/heal [player]` | `sage.heal` | OP | Heal yourself or another player to full health |
| `/xp <amount>` | `sage.xp` | OP | Give yourself or another player experience points |
| `/respawn` | `sage.respawn` | OP | Respawn at your current location instantly |
| `/ping [player]` | `sage.ping` | true | Check your ping or another player's ping |
| `/seen <player>` | `sage.seen` | OP | Check when a player was last seen online |
| `/clear` | `sage.clear` | OP | Clear your entire inventory |
| `/item` | `sage.item` | OP | Browse the item database and get custom items |
| `/openinv <player>` | `sage.openinv` | OP | Open and manage another player's inventory |
| `/openender <player>` | `sage.openender` | OP | Open another player's ender chest |
| `/dispose` | `sage.dispose` | true | Open a disposal GUI to throw away unwanted items |
| `/repair` | `sage.repair` | OP | Repair the item in your hand |
| `/enchantmentbook` | `sage.enchantmentbook` | OP | Create enchantment books with any enchantment and level |
| `/potion` | `sage.potion` | OP | Create custom potion effects |
| `/fly [player] [on/off]` | `sage.fly` | OP | Enable or disable flight for yourself or another player |
| `/speed <speed> [player]` | `sage.speed` | OP | Adjust player movement speed (1-10) |
| `/vanish` | `sage.vanish` | OP | Toggle vanish mode (invisible to regular players) |
| `/ptime <time>` | `sage.ptime` | true | Set your personal day/night time |
| `/pweather <weather>` | `sage.pweather` | true | Set your personal weather (clear, rain, thunder) |
| `/gmc` | `sage.gamemode.creative` | OP | Set your gamemode to Creative |
| `/gms` | `sage.gamemode.survival` | OP | Set your gamemode to Survival |
| `/gma` | `sage.gamemode.adventure` | OP | Set your gamemode to Adventure |
| `/gmsp` | `sage.gamemode.spectator` | OP | Set your gamemode to Spectator |
| `/rules` | `sage.rules` | true | Display the server rules |
| `/uuid [player]` | `sage.uuid` | OP | Get the UUID of yourself or another player |
| `/sudo <player> <command>` | `sage.sudo` | OP | Execute a command as another player |
| `/spawnmob <mob> [amount]` | `sage.spawnmob` | OP | Spawn one or more mobs at your location |
| `/firstjoin` | `sage.firstjoin` | OP | Display the first join message and info |
| `/givesittingstick` | `sage.givesittingstick` | OP | Receive a stick that allows you to sit when right-clicked |
| `/commandspy` | `sage.commandspy` | OP | Toggle spying on all commands executed on the server |
| `/consolespy` | `sage.consolespy` | OP | Toggle spying on console output and commands |
| `/warn <player> [duration] <reason>` | `sage.warn` | OP | Issue a warning to a player |
| `/mute <player> [duration] <reason>` | `sage.mute` | OP | Mute a player, preventing them from chatting |
| `/ban <player> [duration] <reason>` | `sage.ban` | OP | Ban a player from the server |
| `/kick <player> [reason]` | `sage.kick` | OP | Kick a player from the server |
| `/blacklist <player> <reason>` | `sage.blacklist` | OP | Blacklist a player's IP address |
| `/unwarn <player>` | `sage.unwarn` | OP | Remove the most recent warning from a player |
| `/unmute <player>` | `sage.unmute` | OP | Unmute a player |
| `/unban <player>` | `sage.unban` | OP | Unban a player |
| `/unblacklist <player>` | `sage.unblacklist` | OP | Remove a player from the blacklist |
| `/punish <player> <type> [duration] <reason>` | `sage.staff` | OP | Advanced punishment command for applying multiple types |
| `/history [player]` | `sage.history.self` / `sage.history.others` | true / OP | View punishment history for yourself or another player |
| `/chatlogs <player>` | `sage.chatlogs` | OP | View chat logs for a specific player |
| `/broadcast <message>` | `sage.broadcast` | OP | Send a broadcast message to all players |
| `/staffbroadcast <message>` | `sage.staffbroadcast` | OP | Send a broadcast message only to staff members |
| `/staffchat <message>` / `/sc` | `sage.staffchat` | OP | Send a message in staff-only chat |
| `/adminchat <message>` / `/ac` | `sage.adminchat` | OP | Send a message in admin-only chat |
| `/mutechat` | `sage.mutechat` | OP | Toggle global chat mute |
| `/kickall [message]` | `sage.kickall` | OP | Kick all players from the server |
| `/staffmode` | `sage.staffmode` | OP | Toggle staff mode with invisibility and speed boost |
| `/itemdb add <name> [displayname]` | `sage.itemdb.add` | OP | Add the item in your hand to the item database |
| `/itemdb give <player> <item>` | `sage.itemdb.give` | OP | Give a player an item from the database |
| `/itemdb delete <item>` | `sage.itemdb.delete` | OP | Delete an item from the database |
| `/itemdb list` | `sage.itemdb.list` | OP | List all items in the database |
| `/kit` | `sage.kit.claim` | true | Open the kit GUI to browse and claim available kits |
| `/kit claim <kitname>` | `sage.kit.claim` + `sage.kit.<kitname>` | true + OP | Claim a specific kit (with cooldowns) |
| `/kit create <kitname> <duration>` | `sage.kit.create` | OP | Create a new kit with items from your inventory |
| `/kit confirmcreate` | `sage.kit.create` | OP | Confirm kit creation with items from your inventory |
| `/kit cancelcreate` | `sage.kit.create` | OP | Cancel an ongoing kit creation |
| `/kit delete <kitname>` | `sage.kit.delete` | OP | Delete an existing kit |
| `/warp <warpname>` | `sage.warp` / `sage.warp.admin` | OP | Teleport to a named warp location |
| `/warp create <warpname>` | `sage.warp.admin` | OP | Create a new warp at your current location |
| `/warp delete <warpname>` | `sage.warp.admin` | OP | Delete an existing warp |
| `/warp setlocation <warpname>` | `sage.warp.admin` | OP | Update a warp's location to your current position |
| `/warp sethidden <warpname> <true\|false>` | `sage.warp.admin` | OP | Make a warp hidden from regular players |
| `/tpt <player>` | `sage.tpt` | OP | Teleport to a player |
| `/tphere <player>` | `sage.tphere` | OP | Teleport a player to you |
| `/tphereall` | `sage.tphereall` | OP | Teleport all online players to you |
| `/tppos <x> <y> <z> [world]` | `sage.tppos` | OP | Teleport to specific coordinates |
| `/world` | `sage.world` | OP | Open a GUI to teleport to different worlds |

---


### Permission Prefixes

- `sage.` - Main prefix for all Sage plugin permissions
- `sage.kit.*` - Kit-specific permissions
- `sage.gamemode.*` - Gamemode command permissions
- `sage.history.*` - History viewing permissions
- `sage.vanish.*` - Vanish-related permissions
- `sage.mutechat.*` - Chat mute permissions
- `sage.kickall.*` - Kickall bypass permissions

---

## Notes

- **@mentions**: Players can be targeted with `@player` or `@all` in some commands
- **Durations**: Use format like `24h`, `7d`, `30m`, `60s`
- **Colors**: Messages use Minecraft color codes (e.g., `§a` for green, `§c` for red)
- **Configuration**: Punishments and settings can you change in the config.yml file.
- **Cooldowns**: Some commands like `/kit claim` have cooldowns
- **Permissions**: OP by default means server operators have access

---
## License
This project is licensed under the [MIT](https://choosealicense.com/licenses/mit/) license.

---
**Version**: 1.3.0  
