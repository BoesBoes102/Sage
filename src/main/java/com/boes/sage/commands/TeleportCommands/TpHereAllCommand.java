package com.boes.sage.commands.TeleportCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class TpHereAllCommand {

    private final Sage plugin;

    public TpHereAllCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("tphereall [player]")
    @Permission("sage.tphereall")
    public void onCommand(Player sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player destination;

        if (targetName == null) {
            destination = sender;
        } else {
            destination = Bukkit.getPlayer(targetName);
            if (destination == null) {
                sender.sendMessage("§cPlayer is not online!");
                return;
            }
        }

        int teleportedCount = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(destination)) {
                player.teleport(destination.getLocation());
                player.sendMessage("§aYou have been teleported to §e" + destination.getName() + "§a!");
                teleportedCount++;
            }
        }

        if (teleportedCount > 0) {
            sender.sendMessage("§aTeleported §e" + teleportedCount + " §aplayer(s) to §e" + destination.getName() + "§a!");
            if (!sender.equals(destination)) {
                destination.sendMessage("§e" + teleportedCount + " §aplayer(s) have been teleported to you!");
            }
        } else {
            sender.sendMessage("§cNo players to teleport!");
        }
    }
}
