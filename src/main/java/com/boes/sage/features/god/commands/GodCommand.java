package com.boes.sage.features.god.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class GodCommand {

    private final Sage plugin;

    public GodCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("god [player]")
    @Permission("sage.god")
    public void onCommand(
            Player sender,
            @Argument(value = "player", suggestions = "players") String targetName
    ) {
        Player target = sender;

        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            if (!target.equals(sender) && !sender.hasPermission("sage.staff.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to change other players' god mode!");
                return;
            }
        }

        boolean newState = plugin.getGodService().toggleGod(target);

        if (sender.equals(target)) {
            sender.sendMessage(newState ? ChatColor.GREEN + "God mode enabled!" : ChatColor.RED + "God mode disabled!");
        } else {
            target.sendMessage(newState ? ChatColor.GREEN + "Your god mode was enabled!" : ChatColor.RED + "Your god mode was disabled!");
            sender.sendMessage(newState
                ? ChatColor.GREEN + "Enabled god mode for " + target.getName() + "!"
                : ChatColor.RED + "Disabled god mode for " + target.getName() + "!");
        }
    }
}
