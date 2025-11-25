package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("adminchat|ac")
@CommandPermission("sage.adminchat")
public class AdminChatCommand extends BaseCommand {

    private final Sage plugin;

    public AdminChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player, String[] messageArgs) {
        if (messageArgs.length == 0) {
            player.sendMessage("§cUsage: /adminchat <message>");
            return;
        }

        String message = String.join(" ", messageArgs);
        String adminMessage = "§8[§cAdmin Chat§8] §f" + player.getName() + ": §f" + message;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("sage.adminchat")) {
                onlinePlayer.sendMessage(adminMessage);
            }
        }

        Bukkit.getConsoleSender().sendMessage(adminMessage);
    }
}