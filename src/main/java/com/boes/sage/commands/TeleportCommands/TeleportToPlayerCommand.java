package com.boes.sage.commands.TeleportCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class TeleportToPlayerCommand {

    private final Sage plugin;

    public TeleportToPlayerCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("tpt <player>")
    @Permission("sage.tpt")
    public void onCommand(Player issuer, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            if (target.equals(issuer)) {
                issuer.sendMessage("§cYou cannot teleport to yourself!");
                return;
            }

            issuer.teleport(target.getLocation());
            issuer.sendMessage("§aTeleported to §e" + target.getName() + "§a!");
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);

                if (!offlineTarget.hasPlayedBefore()) {
                    Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!issuer.isOnline()) {
                        return;
                    }

                    Location lastLocation = offlineTarget.getPlayer() != null ?
                        offlineTarget.getPlayer().getLocation() :
                        offlineTarget.getBedSpawnLocation();

                    if (lastLocation == null) {
                        lastLocation = Bukkit.getWorlds().getFirst().getSpawnLocation();
                    }

                    issuer.teleport(lastLocation);
                    issuer.sendMessage("§aTeleported to §e" + offlineTarget.getName() + "§a's last known location!");
                });
            });
        }
    }
}
