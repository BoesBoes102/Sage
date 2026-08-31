package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class PingCommand {

    private final Sage plugin;

    public PingCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("ping [player]")
    @Permission("sage.ping")
    public void onCommand(Player sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target;

        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
            target = sender;
        }

        int ping = target.getPing();
        String color;

        if (ping < 50) {
            color = "§a";
        } else if (ping < 100) {
            color = "§2";
        } else if (ping < 150) {
            color = "§e";
        } else if (ping < 250) {
            color = "§6";
        } else {
            color = "§c";
        }

        if (sender.equals(target)) {
            sender.sendMessage("§7Your ping: " + color + ping + "ms");
        } else {
            sender.sendMessage("§7" + target.getName() + "'s ping: " + color + ping + "ms");
        }
    }
}
