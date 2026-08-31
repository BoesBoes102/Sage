package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class RespawnCommand {

    private final Sage plugin;

    public RespawnCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("respawn [player]")
    @Command("kill [player]")
    @Command("die [player]")
    @Permission("sage.respawn")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target;

        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        target.setHealth(0);

        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage("§aYou have been killed!");
        } else {
            sender.sendMessage("§aKilled " + target.getName() + "!");
        }
    }
}
