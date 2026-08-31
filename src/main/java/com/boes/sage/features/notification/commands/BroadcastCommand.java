package com.boes.sage.features.notification.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class BroadcastCommand {

    private final Sage plugin;

    public BroadcastCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("broadcast <message>")
    @Permission("sage.broadcast")
    public void onCommand(
            CommandSender sender,
            @Argument(value = "message", suggestions = "none") @Greedy String message
    ) {
        String processedMessage = ChatColor.translateAlternateColorCodes('&', message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.sendMessage("§6§l" + processedMessage);
            player.sendMessage("");
            player.playSound(player.getLocation(), "block.note_block.ding", 1.0f, 1.0f);
        }
    }
}
