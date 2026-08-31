package com.boes.sage.commands.TeleportCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class TpPosCommand {

    private final Sage plugin;

    public TpPosCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("tppos <x> <y> <z> [world]")
    @Permission("sage.tppos")
    public void onCommand(
            Player player,
            @Argument("x") double x,
            @Argument("y") double y,
            @Argument("z") double z,
            @Argument(value = "world", suggestions = "worldNames") String worldName
    ) {
        World world;
        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage("§cWorld not found: " + worldName);
                return;
            }
        } else {
            world = player.getWorld();
        }

        Location location = new Location(world, x, y, z);
        player.teleport(location);

        player.sendMessage("§aTeleported to " + String.format("%.1f, %.1f, %.1f", x, y, z) +
                " in " + world.getName() + "!");
    }
}
