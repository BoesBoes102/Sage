package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@CommandAlias("firstjoin")
@Description("Check when a player first joined")
@CommandPermission("sage.firstjoin")
public class FirstJoinCommand extends BaseCommand {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a");
    @Default
    @CommandCompletion("@players")
    public void onCommand(CommandSender sender, @Optional String targetName) {
        OfflinePlayer target = null;
        String displayName = null;
        
        if (targetName != null) {
            Player onlinePlayer = Bukkit.getPlayer(targetName);
            if (onlinePlayer != null) {
                target = onlinePlayer;
                displayName = onlinePlayer.getName();
            } else {
                target = Bukkit.getOfflinePlayer(targetName);
                displayName = target.getName();
                if (target.getName() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (OfflinePlayer) sender;
            displayName = ((Player) sender).getName();
        }

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