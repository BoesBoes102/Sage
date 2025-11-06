package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ping")
@Description("Check your ping or another player's ping")
public class PingCommand extends BaseCommand {

    private final Sage plugin;

    public PingCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.ping")
    public void onCommand(Player sender, @Optional Player target) {
        if (target == null) {
            target = sender;
        }

        int ping = target.getPing();
        String color;
        
        if (ping < 50) {
            color = "§a";
        } else if (ping < 100) {
            color = "§2";
        } else if (ping < 150) {
            color = "§e";
        } else if (ping < 250) {
            color = "§6";
        } else {
            color = "§c";
        }

        if (sender.equals(target)) {
            sender.sendMessage("§7Your ping: " + color + ping + "ms");
        } else {
            sender.sendMessage("§7" + target.getName() + "'s ping: " + color + ping + "ms");
        }
    }
}