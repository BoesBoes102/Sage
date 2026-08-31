package com.boes.sage.features.notification.commands;

import com.boes.sage.Sage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class StaffBroadcastCommand {

    private final Sage plugin;

    public StaffBroadcastCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("staffbroadcast <message>")
    @Permission("sage.staffbroadcast")
    public void onCommand(
            CommandSender sender,
            @Argument(value = "message", suggestions = "none") @Greedy String message
    ) {
        String processedMessage = ChatColor.translateAlternateColorCodes('&', message);
        plugin.getNotificationService().sendStaffAnnouncement(sender.getName(), processedMessage);
    }
}
