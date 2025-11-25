package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ping")
@Description("Check your ping or another player's ping")
@CommandPermission("sage.ping")
public class PingCommand extends BaseCommand {

    private final Sage plugin;

    public PingCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @CommandCompletion("@players")
    public void onCommand(Player sender, String[] args) {
        Player target = null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
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