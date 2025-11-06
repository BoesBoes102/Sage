package com.boes.sage.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("tphereall")
@CommandPermission("sage.tphereall")
public class TpHereAllCommand extends BaseCommand {

    private final Sage plugin;

    public TpHereAllCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player sender, @Optional String targetName) {
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