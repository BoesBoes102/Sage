# Sage

Sage is a simple replacement for plugins like CMI & Essentials. It includes essential commands and a staffing system.

## Installation

Download the latest release from the github release page.
Add it to your plugins folder and reload your server.

## Permissions & Configurations

Use a permission handler like Luckperms to handle permissions for every command.
You can customize messages and toggle commands in config.yml, messages.yml, and punishments.yml files.
## Commands & Permissions Quick Reference

| Command | Permission | Default | Description |
|---------|-----------|---------|-------------|
| `/hat` | `sage.hat` | OP | Place item in hand on your head as a hat |
| `/heal [player]` | `sage.heal` | OP | Heal yourself or another player to full health |
| `/feed [player]` | `sage.feed` | OP | Fill hunger and saturation for yourself or another player |
| `/xp <show\|reset\|set\|give> [player] [amount]` | `sage.xp` | OP | View, reset, set, or give player XP levels |
| `/respawn` / `/kill` / `/die [player]` | `sage.respawn` | OP | Respawn or kill yourself or another player instantly |
| `/ping [player]` | `sage.ping` | true | Check your ping or another player's ping |
| `/seen <player>` | `sage.seen` | OP | Check when a player was last seen online |
| `/clear [player]` / `/c` | `sage.clear` | OP | Clear your inventory or another player's |
| `/item <material> [amount]` / `/i` | `sage.item` | OP | Give yourself an item stack |
| `/openinv <player>` / `/invsee` / `/viewinv` | `sage.openinv` | OP | Open and manage another player's inventory |
| `/openender <player>` / `/endersee` / `/viewender` | `sage.openender` | OP | Open another player's ender chest |
| `/ec` / `/enderchest` | `sage.enderchest` | OP | Open your own ender chest |
| `/dispose` / `/thrash` | `sage.dispose` | true | Open a disposal GUI to throw away unwanted items |
| `/repair <mode>` | `sage.repair` | OP | Repair the item in your hand or your full inventory |
| `/enchantmentbook <enchant> <level> <amount>` | `sage.enchantmentbook` | OP | Create enchantment books with any enchantment and level |
| `/potion <type> <effect> <amplifier> <duration> <amount>` | `sage.potion` | OP | Create custom potions with configurable effects |
| `/fly [player] [on/off]` | `sage.fly` | OP | Enable or disable flight for yourself or another player |
| `/speed <speed> [mode] [player]` | `sage.speed` | OP | Adjust player fly or walk speed |
| `/vanish [player]` | `sage.vanish` | OP | Toggle vanish mode (invisible to regular players; requires `sage.vanish.see` to see vanished players) |
| `/ptime <time>` / `/ptime reset` | `sage.ptime` | OP | Set or reset your personal day/night time |
| `/pweather <weather>` / `/pweather reset` | `sage.pweather` | OP | Set or reset your personal weather (clear, rain, thunder) |
| `/gmc` / `/creative` / `/gm2 [player]` | `sage.gamemode.creative` | OP | Set your (or another player's) gamemode to Creative |
| `/gms` / `/survival` / `/gm1 [player]` | `sage.gamemode.survival` | OP | Set your (or another player's) gamemode to Survival |
| `/gma` / `/adventure` / `/gm4 [player]` | `sage.gamemode.adventure` | OP | Set your (or another player's) gamemode to Adventure |
| `/gmsp` / `/spectator` / `/gm3 [player]` | `sage.gamemode.spectator` | OP | Set your (or another player's) gamemode to Spectator |
| `/rules` | `sage.rules` | true | Display the server rules |
| `/uuid [player]` | `sage.uuid` | OP | Get the UUID of yourself or another player |
| `/sudo <player> <true\|false> <command>` | `sage.sudo` | OP | Execute a command as another player, optionally bypassing permissions |
| `/spawnmob <mob> [amount]` | `sage.spawnmob` | OP | Spawn one or more mobs at your location |
| `/firstjoin [player]` | `sage.firstjoin` | OP | Display the first join message and info |
| `/commandspy [on/off]` / `/spycmd` | `sage.commandspy` | OP | Toggle spying on all commands executed on the server |
| `/messagespy [on/off]` / `/msgspy` | `sage.messagespy` | OP | Toggle spying on private messages between players |
| `/back` | `sage.back` | OP | Teleport to your previous location |
| `/freeze [player]` | `sage.freeze` | OP | Freeze a player in place |
| `/god [player]` | `sage.god` | OP | Toggle invulnerability for yourself or another player |
| `/alts <player>` | `sage.alts` | OP | Show a player's known alt accounts |
| `/altsall <player>` | `sage.altsall` | OP | Show all alt accounts recursively |
| `/msg <player> <message>` / `/message` / `/tell` / `/w` / `/whisper` / `/pm` | `sage.msg` | true | Send a private message to another player |
| `/reply <message>` / `/r` | `sage.msg` | true | Reply to the last private message received |
| `/refund [player]` | none / `sage.refund.admin` for others | OP | View your own claimable refunds, or (with permission) another player's |
| `/usage [ram\|cpu\|bar]` | `sage.usage` | OP | Show server RAM/CPU usage stats |
| `/itemedit` / `/ie` | `sage.itemedit` | OP | Edit the item in hand (rename, lore, enchants, attributes, flags, banners, potions, books, and more) |
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
| `/history [player]` | `sage.history.self` / `sage.history.others` | OP / OP | View punishment history for yourself or another player |
| `/chatlogs <player>` | `sage.chatlogs` | OP | View chat logs for a specific player |
| `/broadcast <message>` | `sage.broadcast` | OP | Send a broadcast message to all players |
| `/staffbroadcast <message>` | `sage.staffbroadcast` | OP | Send a broadcast message only to staff members |
| `/staffchat <message>` / `/sc` | `sage.staffchat` | OP | Send a message in staff-only chat |
| `/adminchat <message>` / `/ac` | `sage.adminchat` | OP | Send a message in admin-only chat |
| `/mutechat` | `sage.mutechat` | OP | Toggle global chat mute |
| `/kickall [message]` | `sage.kickall` | OP | Kick all players from the server |
| `/staffmode` / `/h` | `sage.staffmode` | OP | Toggle staff mode with invisibility and speed boost |
| `/itemdb add <name> [displayname]` | `sage.itemdb.add` | OP | Add the item in your hand to the item database |
| `/itemdb give <player> <item>` | `sage.itemdb.give` | OP | Give a player an item from the database |
| `/itemdb delete <item>` | `sage.itemdb.delete` | OP | Delete an item from the database |
| `/itemdb list` | `sage.itemdb.list` | OP | List all items in the database |
| `/kit` / `/kit gui` | none | true | Open the kit GUI to browse and claim available kits |
| `/kit claim <kitname>` | `sage.kit.<kitname>` | OP | Claim a specific kit (with cooldowns) |
| `/kit create <kitname> <duration>` | `sage.kit.create` | OP | Create a new kit with items from your inventory |
| `/kit confirmcreate` | `sage.kit.create` | OP | Confirm kit creation with items from your inventory |
| `/kit cancelcreate` | `sage.kit.create` | OP | Cancel an ongoing kit creation |
| `/kit delete <kitname>` | `sage.kit.delete` | OP | Delete an existing kit |
| `/givekit <player> <kitname>` | `sage.kit.give` | OP | Give a kit directly to a player, bypassing its cooldown |
| `/warp [warpname]` | `sage.warp` / `sage.warp.admin` | OP | Teleport to a named warp, or list warps |
| `/warp create <warpname>` | `sage.warp.admin` | OP | Create a new warp at your current location |
| `/warp delete <warpname>` | `sage.warp.admin` | OP | Delete an existing warp |
| `/warp setlocation <warpname>` | `sage.warp.admin` | OP | Update a warp's location to your current position |
| `/warp sethidden <warpname> <true\|false>` | `sage.warp.admin` | OP | Make a warp hidden from regular players |
| `/tpt <player>` | `sage.tpt` | OP | Teleport to a player |
| `/tphere <player>` | `sage.tphere` | OP | Teleport a player to you |
| `/tphereall [player]` | `sage.tphereall` | OP | Teleport all online players to you |
| `/tppos <x> <y> <z> [world]` | `sage.tppos` | OP | Teleport to specific coordinates |
| `/world [world]` | `sage.world` | OP | List available worlds and teleport to a loaded or unloaded world by name |
| `/craft` / `/workbench` / `/craftingtable` | `sage.craft` | OP | Open a virtual crafting table |
| `/stonecutter` | `sage.stonecutter` | OP | Open a virtual stonecutter |
| `/loom` | `sage.loom` | OP | Open a virtual loom |
| `/cartographytable` / `/cartography` | `sage.cartographytable` | OP | Open a virtual cartography table |
| `/smithingtable` / `/smith` | `sage.smithingtable` | OP | Open a virtual smithing table |
| `/anvil` | `sage.anvil` | OP | Open a virtual anvil |
| `/grindstone` | `sage.grindstone` | OP | Open a virtual grindstone |
| `/furnace` | `sage.furnace` | OP | Open a virtual furnace |
| `/blastfurnace` | `sage.blastfurnace` | OP | Open a virtual blast furnace |
| `/smoker` | `sage.smoker` | OP | Open a virtual smoker |
| `/brewingstand` | `sage.brewingstand` | OP | Open a virtual brewing stand |

---
## License
This project is licensed under the [MIT](https://choosealicense.com/licenses/mit/) license.

---
**Version**: 1.6.0  
