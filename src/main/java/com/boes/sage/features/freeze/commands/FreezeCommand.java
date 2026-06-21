package com.boes.sage.features.freeze.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("freeze")
@CommandPermission("sage.freeze")
public class FreezeCommand extends BaseCommand {
    private final Sage plugin;

    public FreezeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("[player]")
    public void onCommand(Player player, @Optional String targetName) {
        if (targetName == null) {
            if (plugin.getFreezeService().isFrozen(player) && player.hasPermission("*")) {
                plugin.getFreezeService().unfreeze(player);
                player.sendMessage(ChatColor.GREEN + "You have been unfrozen.");
                return;
            }

            player.sendMessage(ChatColor.RED + "You can only freeze other players.");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            if (plugin.getFreezeService().isFrozen(player) && player.hasPermission("*")) {
                plugin.getFreezeService().unfreeze(player);
                player.sendMessage(ChatColor.GREEN + "You have been unfrozen.");
                return;
            }

            player.sendMessage(ChatColor.RED + "You can only freeze other players.");
            return;
        }

        boolean frozen = plugin.getFreezeService().toggleFreeze(target);
        if (frozen) {
            player.sendMessage(ChatColor.GREEN + target.getName() + " has been frozen.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + target.getName() + " has been unfrozen.");
    }
}
