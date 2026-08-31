package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class GamemodeCreativeCommand {

    private final Sage plugin;

    public GamemodeCreativeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("creative [player]")
    @Command("gmc [player]")
    @Command("gm2 [player]")
    @Permission("sage.gamemode.creative")
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

        target.setGameMode(GameMode.CREATIVE);

        if (sender.equals(target)) {
            target.sendMessage("§aYour gamemode has been set to Creative!");
        } else {
            target.sendMessage("§aYour gamemode has been set to Creative!");
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to Creative!");
        }
    }
}
