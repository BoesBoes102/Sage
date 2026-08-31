package com.boes.sage.features.back.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.back.BackService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class BackCommand {

    private final BackService backService;

    public BackCommand(Sage plugin) {
        this.backService = plugin.getBackService();
    }

    @Command("back")
    @Permission("sage.back")
    public void onCommand(Player player) {
        Location lastLocation = backService.getLastLocation(player);

        if (lastLocation == null || lastLocation.getWorld() == null) {
            player.sendMessage("§cYou have no previous location to go back to!");
            return;
        }

        backService.suppressNextRecord(player.getUniqueId());
        Location currentLocation = player.getLocation();
        player.teleport(lastLocation);
        backService.setLastLocation(player, currentLocation);

        player.sendMessage("§aTeleported back to your previous location!");
    }
}
