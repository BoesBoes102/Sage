package com.boes.sage.features.messaging.commands;

import com.boes.sage.Sage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class MessageCommand {

    private final Sage plugin;

    public MessageCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("msg <player> <message>")
    @Command("message <player> <message>")
    @Command("tell <player> <message>")
    @Command("w <player> <message>")
    @Command("whisper <player> <message>")
    @Command("pm <player> <message>")
    @Permission("sage.msg")
    public void onCommand(
            Player sender,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument(value = "message", suggestions = "none") @Greedy String message
    ) {
        plugin.getMessagingService().sendPrivateMessage(sender, targetName, message);
    }
}
