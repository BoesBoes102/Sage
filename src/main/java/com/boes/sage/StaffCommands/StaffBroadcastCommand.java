package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("staffbroadcast")
@CommandPermission("sage.staffbroadcast")
public class StaffBroadcastCommand extends BaseCommand {

    private final Sage plugin;

    public StaffBroadcastCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(org.bukkit.command.CommandSender sender, String[] messageArgs) {
        if (messageArgs.length == 0) {
            sender.sendMessage("§cUsage: /staffbroadcast <message>");
            return;
        }

        String message = String.join(" ", messageArgs);
        String processedMessage = ChatColor.translateAlternateColorCodes('&', message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("sage.staffmode")) {
                player.sendMessage("");
                player.sendMessage("§5§l[STAFF] " + processedMessage);
                player.sendMessage("");
                player.playSound(player.getLocation(), "block.note_block.ding", 1.0f, 1.0f);
            }
        }
    }
}