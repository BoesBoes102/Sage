package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.punishment.gui.HistoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class HistoryCommand {

    private final Sage plugin;

    public HistoryCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("history [player]")
    @Command("hist [player]")
    public void onCommand(
            Player issuer,
            @Argument(value = "player", suggestions = "players") String targetName
    ) {
        if (targetName == null) {
            if (!issuer.hasPermission("sage.history.self")) {
                issuer.sendMessage("§cYou don't have permission!");
                return;
            }
            new HistoryGUI(plugin, issuer, issuer).open();
            return;
        }

        if (!issuer.hasPermission("sage.history.others")) {
            issuer.sendMessage("§cYou don't have permission to view others' history!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!issuer.isOnline()) {
                    return;
                }
                new HistoryGUI(plugin, issuer, target).open();
            });
        });
    }
}
