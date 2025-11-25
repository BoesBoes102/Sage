package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@CommandAlias("seen")
@Description("Check when a player was last seen")
@CommandPermission("sage.seen")
public class SeenCommand extends BaseCommand {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss");
    @Default
    @CommandCompletion("@players")
    public void onCommand(org.bukkit.command.CommandSender sender, String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            sender.sendMessage("§a" + onlinePlayer.getName() + " is currently online!");
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage("§cPlayer " + playerName + " has never joined the server!");
            return;
        }

        long lastPlayed = offlinePlayer.getLastPlayed();
        Date lastPlayedDate = new Date(lastPlayed);
        String formattedDate = dateFormat.format(lastPlayedDate);
        
        long timeDiff = System.currentTimeMillis() - lastPlayed;
        String timeAgo = formatTimeDifference(timeDiff);

        sender.sendMessage("§e" + offlinePlayer.getName() + " §7was last seen:");
        sender.sendMessage("§7" + formattedDate);
        sender.sendMessage("§7(" + timeAgo + " ago)");
    }

    private String formatTimeDifference(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "");
        }
    }
}