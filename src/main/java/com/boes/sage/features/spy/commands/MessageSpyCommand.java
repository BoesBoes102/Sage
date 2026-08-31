package com.boes.sage.features.spy.commands;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class MessageSpyCommand {

    private final Sage plugin;

    public MessageSpyCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("messagespy [state]")
    @Command("msgspy [state]")
    @Permission("sage.messagespy")
    public void onCommand(
            Player player,
            @Argument("state") String state
    ) {
        if (state == null) {
            boolean newState = plugin.getMessagingService().toggleMessageSpy(player);
            player.sendMessage(newState ? "§aMessage spy enabled!" : "§cMessage spy disabled!");
            return;
        }

        state = state.toLowerCase();
        if (state.equals("on") || state.equals("true")) {
            plugin.getMessagingService().setMessageSpy(player, true);
            player.sendMessage("§aMessage spy enabled!");
        } else if (state.equals("off") || state.equals("false")) {
            plugin.getMessagingService().setMessageSpy(player, false);
            player.sendMessage("§cMessage spy disabled!");
        } else {
            player.sendMessage("§cUsage: /messagespy <on/off>");
        }
    }
}
