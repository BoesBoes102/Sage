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

public class FirstJoinCommand {
    private final Sage plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a");

    public FirstJoinCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("firstjoin [player]")
    @Permission("sage.firstjoin")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String targetName) {
        if (targetName != null) {
            Player onlinePlayer = Bukkit.getPlayer(targetName);
            if (onlinePlayer != null) {
                reportFirstJoin(sender, onlinePlayer, onlinePlayer.getName());
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                String displayName = target.getName();
                if (displayName == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                    Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("§cPlayer not found!"));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> reportFirstJoin(sender, target, displayName));
            });
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must specify a player from console!");
            return;
        }
        OfflinePlayer target = (OfflinePlayer) sender;
        String displayName = ((Player) sender).getName();
        reportFirstJoin(sender, target, displayName);
    }

    private void reportFirstJoin(CommandSender sender, OfflinePlayer target, String displayName) {
        long firstPlayed = target.getFirstPlayed();

        if (firstPlayed == 0) {
            sender.sendMessage("§cNo first join data available for this player!");
            return;
        }

        Date firstJoinDate = new Date(firstPlayed);
        String formattedDate = dateFormat.format(firstJoinDate);

        long timeSince = System.currentTimeMillis() - firstPlayed;
        String timeSinceStr = formatTimeSince(timeSince);

        sender.sendMessage("§7§m                                                    ");
        sender.sendMessage("§6§lFirst Join Information");
        sender.sendMessage("");
        sender.sendMessage("§ePlayer: §f" + displayName);
        sender.sendMessage("§eFirst Joined: §f" + formattedDate);
        sender.sendMessage("§eTime Since: §f" + timeSinceStr);
        sender.sendMessage("§7§m                                                    ");
    }

    private String formatTimeSince(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        } else if (months > 0) {
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        } else if (weeks > 0) {
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        } else if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
        }
    }
}
