package com.boes.sage.commands.TeleportCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class TpHereCommand {

    private final Sage plugin;

    public TpHereCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("tphere <player>")
    @Permission("sage.tphere")
    public void onCommand(Player issuer, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            if (target.equals(issuer)) {
                issuer.sendMessage("§cYou cannot teleport yourself to yourself!");
                return;
            }

            target.teleport(issuer.getLocation());
            issuer.sendMessage("§aTeleported §e" + target.getName() + " §ato you!");
            target.sendMessage("§aYou have been teleported to §e" + issuer.getName() + "§a!");
        } else {
            String pendingTeleport = issuer.getLocation().getWorld().getName() + ";" +
                issuer.getLocation().getX() + ";" +
                issuer.getLocation().getY() + ";" +
                issuer.getLocation().getZ() + ";" +
                issuer.getLocation().getYaw() + ";" +
                issuer.getLocation().getPitch();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);

                if (!offlineTarget.hasPlayedBefore()) {
                    Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                    return;
                }

                plugin.getPlayerRuntimeDataManager().setPendingTeleport(offlineTarget.getUniqueId(), pendingTeleport);

                Bukkit.getScheduler().runTask(plugin, () ->
                        issuer.sendMessage("§aTeleport queued for §e" + offlineTarget.getName() + " §a(offline). They will be teleported when they log in."));
            });
        }
    }
}
