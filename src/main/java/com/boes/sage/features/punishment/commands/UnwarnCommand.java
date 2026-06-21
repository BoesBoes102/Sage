package com.boes.sage.features.punishment.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.features.punishment.data.PunishmentHistory;
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
    @Syntax("<player> [number]")
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName, @Optional Integer warnNumber) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("\u00A7cPlayer has never joined!");
            return;
        }

        List<PunishmentHistory> warnings = plugin.getPunishmentService().getHistoryByType(target.getUniqueId(), "warn");

        if (warnings.isEmpty()) {
            player.sendMessage("\u00A7c" + target.getName() + " has no warnings!");
            return;
        }

        if (warnNumber != null) {
            if (warnNumber < 1 || warnNumber > warnings.size()) {
                player.sendMessage("\u00A7cWarning number must be between 1 and " + warnings.size() + "!");
                return;
            }

            if (plugin.getPunishmentService().removeHistoryByIndex(target.getUniqueId(), "warn", warnNumber - 1)) {
                player.sendMessage("\u00A7aRemoved warning " + warnNumber + " from " + target.getName());
                plugin.getNotificationService().sendStaffNotice("\u00A7e" + player.getName() + " \u00A77removed warning \u00A7e" + warnNumber + " \u00A77from \u00A7e" + target.getName());
            } else {
                player.sendMessage("\u00A7cFailed to remove warning!");
            }
            return;
        }

        player.sendMessage("\u00A7e" + target.getName() + "'s Warnings:");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < warnings.size(); i++) {
            PunishmentHistory warning = warnings.get(i);
            String date = sdf.format(new Date(warning.getTimestamp()));
            player.sendMessage("\u00A77[\u00A7f" + (i + 1) + "\u00A77] \u00A7fReason: \u00A7e" + warning.getReason() +
                " \u00A77| \u00A7fDate: \u00A7e" + date);
        }

        player.sendMessage("\u00A77Use: /unwarn " + target.getName() + " <number>");
    }
}
