package com.boes.sage.commands.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("tphere")
@CommandPermission("sage.tphere")
public class TpHereCommand extends BaseCommand {

    private final Sage plugin;

    public TpHereCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            if (target.equals(player)) {
                player.sendMessage("§cYou cannot teleport yourself to yourself!");
                return;
            }

            target.teleport(player.getLocation());
            player.sendMessage("§aTeleported §e" + target.getName() + " §ato you!");
            target.sendMessage("§aYou have been teleported to §e" + player.getName() + "§a!");
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!offlineTarget.hasPlayedBefore()) {
                player.sendMessage("§cPlayer has never joined!");
                return;
            }

            plugin.getConfig().set("pending-teleports." + offlineTarget.getUniqueId(),
                player.getLocation().getWorld().getName() + ";" +
                player.getLocation().getX() + ";" +
                player.getLocation().getY() + ";" +
                player.getLocation().getZ() + ";" +
                player.getLocation().getYaw() + ";" +
                player.getLocation().getPitch());
            plugin.saveConfig();
            
            player.sendMessage("§aTeleport queued for §e" + offlineTarget.getName() + " §a(offline). They will be teleported when they log in.");
        }
    }
}