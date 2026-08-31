package com.boes.sage.commands.StaffCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.ArrayList;
import java.util.List;

public class KickAllCommand {

    private final Sage plugin;

    public KickAllCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("kickall [reason]")
    @Permission("sage.kickall")
    public void onCommand(Player sender, @Argument("reason") @Greedy String reasonInput) {
        String reason = (reasonInput != null && !reasonInput.isEmpty())
                ? reasonInput
                : "No Reason Given";

        List<Player> playersToKick = new ArrayList<>(Bukkit.getOnlinePlayers());

        int kickedCount = 0;
        for (Player player : playersToKick) {
            if (player.equals(sender)) {
                continue;
            }

            if (player.hasPermission("sage.kickall.bypass")) {
                continue;
            }

            player.kickPlayer("§c§lKICKED\n§7Reason: §f" + reason);
            kickedCount++;
        }

        sender.sendMessage("§aKicked §e" + kickedCount + " §aplayer(s) for: §e" + reason);
    }
}
