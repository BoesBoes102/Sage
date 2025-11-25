package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("broadcast")
@CommandPermission("sage.broadcast")
public class BroadcastCommand extends BaseCommand {

    private final Sage plugin;

    public BroadcastCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(org.bukkit.command.CommandSender sender, String[] messageArgs) {
        if (messageArgs.length == 0) {
            sender.sendMessage("§cUsage: /broadcast <message>");
            return;
        }

        String message = String.join(" ", messageArgs);
        String processedMessage = ChatColor.translateAlternateColorCodes('&', message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.sendMessage("§6§l" + processedMessage);
            player.sendMessage("");
            player.playSound(player.getLocation(), "block.note_block.ding", 1.0f, 1.0f);
        }
    }
}