package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.Objects;

public class HealCommand {

    private final Sage plugin;

    public HealCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("heal [player]")
    @Permission("sage.heal")
    public void onCommand(Player sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target;

        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
            target = sender;
        }

        target.setHealth(Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue());
        target.setFoodLevel(20);
        target.setSaturation(20.0f);
        target.setFireTicks(0);

        if (sender.equals(target)) {
            target.sendMessage("§aYou have been healed!");
        } else {
            target.sendMessage("§aYou have been healed by " + sender.getName() + "!");
            sender.sendMessage("§aHealed " + target.getName() + "!");
        }
    }
}
