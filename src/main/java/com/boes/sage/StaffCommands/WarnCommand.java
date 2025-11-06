package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("warn")
@CommandPermission("sage.warn")
public class WarnCommand extends BaseCommand {

    private final Sage plugin;

    public WarnCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName, String[] reason) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        String reasonText = String.join(" ", reason);
        plugin.getPunishmentManager().warn(target, reasonText, player);
        player.sendMessage("§aWarned §e" + target.getName() + " §afor: §f" + reasonText);
    }
}