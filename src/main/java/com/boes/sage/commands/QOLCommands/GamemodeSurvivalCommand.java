package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class GamemodeSurvivalCommand {

    private final Sage plugin;

    public GamemodeSurvivalCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("survival [player]")
    @Command("gms [player]")
    @Command("gm1 [player]")
    @Permission("sage.gamemode.survival")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target = null;

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

        target.setGameMode(GameMode.SURVIVAL);

        if (sender.equals(target)) {
            target.sendMessage("§aYour gamemode has been set to Survival!");
        } else {
            target.sendMessage("§aYour gamemode has been set to Survival!");
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to Survival!");
        }
    }
}
