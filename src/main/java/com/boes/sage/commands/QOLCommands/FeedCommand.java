package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class FeedCommand {

    private final Sage plugin;

    public FeedCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("feed [player]")
    @Permission("sage.feed")
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

        target.setFoodLevel(20);
        target.setSaturation(20.0f);

        if (sender.equals(target)) {
            target.sendMessage("§aYou have been fed!");
        } else {
            target.sendMessage("§aYou have been fed by " + sender.getName() + "!");
            sender.sendMessage("§aFed " + target.getName() + "!");
        }
    }
}
