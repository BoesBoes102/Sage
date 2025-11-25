package com.boes.sage.commands.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("tpt")
@CommandPermission("sage.tpt")
public class TeleportToPlayerCommand extends BaseCommand {

    private final Sage plugin;

    public TeleportToPlayerCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            if (target.equals(player)) {
                player.sendMessage("§cYou cannot teleport to yourself!");
                return;
            }

            player.teleport(target.getLocation());
            player.sendMessage("§aTeleported to §e" + target.getName() + "§a!");
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!offlineTarget.hasPlayedBefore()) {
                player.sendMessage("§cPlayer has never joined!");
                return;
            }

            Location lastLocation = offlineTarget.getPlayer() != null ? 
                offlineTarget.getPlayer().getLocation() : 
                offlineTarget.getBedSpawnLocation();
            
            if (lastLocation == null) {
                lastLocation = Bukkit.getWorlds().getFirst().getSpawnLocation();
            }
            
            player.teleport(lastLocation);
            player.sendMessage("§aTeleported to §e" + offlineTarget.getName() + "§a's last known location!");
        }
    }
}