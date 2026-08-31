package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class UnbanCommand {

    private final Sage plugin;

    public UnbanCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("unban <player>")
    @Permission("sage.unban")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "bans") String targetName
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            if (!plugin.getPunishmentService().isAlreadyBanned(target)) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§c" + target.getName() + " is not banned!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getBanList(BanList.Type.NAME).pardon(target.getName());

                if (target.isOnline()) {
                    String ip = target.getPlayer().getAddress().getAddress().getHostAddress();
                    Bukkit.getBanList(BanList.Type.IP).pardon(ip);
                } else {
                    String storedIP = plugin.getPunishmentService().getBannedPlayerIP(target.getUniqueId());
                    if (storedIP != null) {
                        Bukkit.getBanList(BanList.Type.IP).pardon(storedIP);
                    }
                }

                plugin.getPunishmentService().removeBannedPlayerIP(target.getUniqueId());

                issuer.sendMessage("§aUnbanned " + target.getName());
                plugin.getNotificationService().sendStaffNotice("§e" + issuer.getName() + " §7unbanned §e" + target.getName());
            });
        });
    }
}
