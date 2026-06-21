package com.boes.sage.features.notification.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.entity.Player;

@CommandAlias("staffchat|sc")
@CommandPermission("sage.staffchat")
public class StaffChatCommand extends BaseCommand {

    private final Sage plugin;

    public StaffChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<message>")
    public void onCommand(Player player, String[] messageArgs) {
        if (messageArgs.length == 0) {
            player.sendMessage("\u00A7cUsage: /staffchat <message>");
            return;
        }

        plugin.getNotificationService().sendStaffChat(player, String.join(" ", messageArgs));
    }
}
