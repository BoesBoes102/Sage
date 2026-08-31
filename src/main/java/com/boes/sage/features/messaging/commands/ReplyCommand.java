package com.boes.sage.features.messaging.commands;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class ReplyCommand {

    private final Sage plugin;

    public ReplyCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("reply <message>")
    @Command("r <message>")
    @Permission("sage.msg")
    public void onCommand(
            Player sender,
            @Argument(value = "message", suggestions = "none") @Greedy String message
    ) {
        plugin.getMessagingService().replyToLastMessage(sender, message);
    }
}
