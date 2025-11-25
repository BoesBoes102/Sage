package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("kickall")
@CommandPermission("sage.kickall")
public class KickAllCommand extends BaseCommand {

    private final Sage plugin;

    public KickAllCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player sender, @Optional String[] reasonArgs) {
        String reason = (reasonArgs != null && reasonArgs.length > 0)
                ? String.join(" ", reasonArgs)
                : "Server maintenance";

        List<Player> playersToKick = new ArrayList<>(Bukkit.getOnlinePlayers());

        int kickedCount = 0;
        for (Player player : playersToKick) {
            if (player.equals(sender)) {
                continue;
            }

            if (player.hasPermission("sage.kickall.bypass")) {
                continue;
            }

            player.kickPlayer("§c" + reason);
            kickedCount++;
        }

        sender.sendMessage("§aKicked §e" + kickedCount + " §aplayer(s) for: §e" + reason);
    }
}