package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("staffchat|sc")
@CommandPermission("sage.staffchat")
public class StaffChatCommand extends BaseCommand {

    private final Sage plugin;

    public StaffChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player, String message) {
        if (message == null || message.isEmpty()) {
            player.sendMessage("§cUsage: /staffchat <message>");
            return;
        }

        String staffMessage = "§8[§bSC§8] §f" + player.getName() + ": §f" + message;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("sage.staffchat")) {
                onlinePlayer.sendMessage(staffMessage);
            }
        }

        Bukkit.getConsoleSender().sendMessage(staffMessage);
    }
}