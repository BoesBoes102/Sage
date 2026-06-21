package com.boes.sage.features.punishment.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.features.punishment.gui.HistoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("history|hist")
public class HistoryCommand extends BaseCommand {

    private final Sage plugin;

    public HistoryCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onCommand(Player player, @Optional String targetName) {
        if (targetName == null) {
            if (!player.hasPermission("sage.history.self")) {
                player.sendMessage("§cYou don't have permission!");
                return;
            }
            new HistoryGUI(plugin, player, player).open();
            return;
        }

        if (!player.hasPermission("sage.history.others")) {
            player.sendMessage("§cYou don't have permission to view others' history!");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        new HistoryGUI(plugin, player, target).open();
    }
}
