package com.boes.sage.features.notification.commands;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class AdminChatCommand {

    private final Sage plugin;

    public AdminChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("adminchat <message>")
    @Command("ac <message>")
    @Permission("sage.adminchat")
    public void onCommand(
            Player player,
            @Argument(value = "message", suggestions = "none") @Greedy String message
    ) {
        plugin.getNotificationService().sendAdminChat(player, message);
    }
}
