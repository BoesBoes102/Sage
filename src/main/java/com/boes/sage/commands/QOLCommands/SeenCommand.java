package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SeenCommand {

    private final Sage plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss");

    public SeenCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("seen <player>")
    @Permission("sage.seen")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            sender.sendMessage("§a" + onlinePlayer.getName() + " is currently online!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

            if (!offlinePlayer.hasPlayedBefore()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage("§cPlayer " + playerName + " has never joined the server!"));
                return;
            }

            long lastPlayed = offlinePlayer.getLastPlayed();
            Date lastPlayedDate = new Date(lastPlayed);
            String formattedDate = dateFormat.format(lastPlayedDate);

            long timeDiff = System.currentTimeMillis() - lastPlayed;
            String timeAgo = formatTimeDifference(timeDiff);

            String offlineName = offlinePlayer.getName();

            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage("§e" + offlineName + " §7was last seen:");
                sender.sendMessage("§7" + formattedDate);
                sender.sendMessage("§7(" + timeAgo + " ago)");
            });
        });
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
