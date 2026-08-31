package com.boes.sage.features.notification.commands;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class StaffChatCommand {

    private final Sage plugin;

    public StaffChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("staffchat <message>")
    @Command("sc <message>")
    @Permission("sage.staffchat")
    public void onCommand(
            Player player,
            @Argument(value = "message", suggestions = "none") @Greedy String message
    ) {
        plugin.getNotificationService().sendStaffChat(player, message);
    }
}
