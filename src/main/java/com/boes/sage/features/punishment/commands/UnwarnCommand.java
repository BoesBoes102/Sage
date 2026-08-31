package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.punishment.data.PunishmentHistory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UnwarnCommand {

    private final Sage plugin;

    public UnwarnCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("unwarn <player> [number]")
    @Permission("sage.unwarn")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument(value = "number", suggestions = "none") Integer warnNumber
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            List<PunishmentHistory> warnings = plugin.getPunishmentService().getHistoryByType(target.getUniqueId(), "warn");

            if (warnings.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§c" + target.getName() + " has no warnings!"));
                return;
            }

            if (warnNumber != null) {
                if (warnNumber < 1 || warnNumber > warnings.size()) {
                    int size = warnings.size();
                    Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cWarning number must be between 1 and " + size + "!"));
                    return;
                }

                boolean removed = plugin.getPunishmentService().removeHistoryByIndex(target.getUniqueId(), "warn", warnNumber - 1);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (removed) {
                        issuer.sendMessage("§aRemoved warning " + warnNumber + " from " + target.getName());
                        plugin.getNotificationService().sendStaffNotice("§e" + issuer.getName() + " §7removed warning §e" + warnNumber + " §7from §e" + target.getName());
                    } else {
                        issuer.sendMessage("§cFailed to remove warning!");
                    }
                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                issuer.sendMessage("§e" + target.getName() + "'s Warnings:");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                for (int i = 0; i < warnings.size(); i++) {
                    PunishmentHistory warning = warnings.get(i);
                    String date = sdf.format(new Date(warning.getTimestamp()));
                    issuer.sendMessage("§7[§f" + (i + 1) + "§7] §fReason: §e" + warning.getReason() +
                        " §7| §fDate: §e" + date);
                }

                issuer.sendMessage("§7Use: /unwarn " + target.getName() + " <number>");
            });
        });
    }
}
