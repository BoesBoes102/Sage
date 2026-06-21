package com.boes.sage.features.notification.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("staffbroadcast")
@CommandPermission("sage.staffbroadcast")
public class StaffBroadcastCommand extends BaseCommand {

    private final Sage plugin;

    public StaffBroadcastCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<message>")
    public void onCommand(CommandSender sender, String[] messageArgs) {
        if (messageArgs.length == 0) {
            sender.sendMessage("\u00A7cUsage: /staffbroadcast <message>");
            return;
        }

        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", messageArgs));
        plugin.getNotificationService().sendStaffAnnouncement(sender.getName(), message);
    }
}
