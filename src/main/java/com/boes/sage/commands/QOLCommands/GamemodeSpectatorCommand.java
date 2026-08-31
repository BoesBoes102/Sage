package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class GamemodeSpectatorCommand {

    private final Sage plugin;

    public GamemodeSpectatorCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("spectator [player]")
    @Command("gmsp [player]")
    @Command("gm3 [player]")
    @Permission("sage.gamemode.spectator")
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

        target.setGameMode(GameMode.SPECTATOR);

        if (sender.equals(target)) {
            target.sendMessage("§aYour gamemode has been set to Spectator!");
        } else {
            target.sendMessage("§aYour gamemode has been set to Specatator!");
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to Spectator!");
        }
    }
}
