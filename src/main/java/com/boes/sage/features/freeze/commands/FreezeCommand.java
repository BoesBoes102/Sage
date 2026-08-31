package com.boes.sage.features.freeze.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class FreezeCommand {
    private final Sage plugin;

    public FreezeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("freeze [player]")
    @Permission("sage.freeze")
    public void onCommand(
            Player issuer,
            @Argument(value = "player", suggestions = "players") String targetName
    ) {
        if (targetName == null) {
            if (plugin.getFreezeService().isFrozen(issuer) && issuer.hasPermission("*")) {
                plugin.getFreezeService().unfreeze(issuer);
                issuer.sendMessage(ChatColor.GREEN + "You have been unfrozen.");
                return;
            }

            issuer.sendMessage(ChatColor.RED + "You can only freeze other players.");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            issuer.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (target.getUniqueId().equals(issuer.getUniqueId())) {
            if (plugin.getFreezeService().isFrozen(issuer) && issuer.hasPermission("*")) {
                plugin.getFreezeService().unfreeze(issuer);
                issuer.sendMessage(ChatColor.GREEN + "You have been unfrozen.");
                return;
            }

            issuer.sendMessage(ChatColor.RED + "You can only freeze other players.");
            return;
        }

        boolean frozen = plugin.getFreezeService().toggleFreeze(target);
        if (frozen) {
            issuer.sendMessage(ChatColor.GREEN + target.getName() + " has been frozen.");
            return;
        }

        issuer.sendMessage(ChatColor.GREEN + target.getName() + " has been unfrozen.");
    }
}
