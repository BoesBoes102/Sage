package com.boes.sage.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@CommandAlias("tppos")
@CommandPermission("sage.tppos")
public class TpPosCommand extends BaseCommand {

    private final Sage plugin;

    public TpPosCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@worlds")
    public void onCommand(Player player, double x, double y, double z, @Optional String worldName) {
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