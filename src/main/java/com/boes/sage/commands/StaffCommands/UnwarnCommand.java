package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import com.boes.sage.data.PunishmentHistory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CommandAlias("unwarn")
@CommandPermission("sage.unwarn")
public class UnwarnCommand extends BaseCommand {

    private final Sage plugin;

    public UnwarnCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName, @Optional Integer warnNumber) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        List<PunishmentHistory> warnings = plugin.getPunishmentManager().getHistoryByType(target.getUniqueId(), "warn");

        if (warnings.isEmpty()) {
            player.sendMessage("§c" + target.getName() + " has no warnings!");
            return;
        }

        if (warnNumber != null) {
            if (warnNumber < 1 || warnNumber > warnings.size()) {
                player.sendMessage("§cWarning number must be between 1 and " + warnings.size() + "!");
                return;
            }
            
            if (plugin.getPunishmentManager().removeHistoryByIndex(target.getUniqueId(), "warn", warnNumber - 1)) {
                player.sendMessage("§aRemoved warning " + warnNumber + " from " + target.getName());

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("sage.staff")) {
                        p.sendMessage("§e" + player.getName() + " §7removed warning §e" + warnNumber + " §7from §e" + target.getName());
                    }
                }
            } else {
                player.sendMessage("§cFailed to remove warning!");
            }
        } else {
            player.sendMessage("§e" + target.getName() + "'s Warnings:");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < warnings.size(); i++) {
                PunishmentHistory warning = warnings.get(i);
                String date = sdf.format(new Date(warning.getTimestamp()));
                player.sendMessage("§7[§f" + (i + 1) + "§7] §fReason: §e" + warning.getReason() +
                        " §7| §fDate: §e" + date);
            }

            player.sendMessage("§7Use: /unwarn " + target.getName() + " <number>");
        }
    }
}