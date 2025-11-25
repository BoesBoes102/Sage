package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

@CommandAlias("heal")
@Description("Heal yourself or another player")
@CommandPermission("sage.heal")
public class HealCommand extends BaseCommand {

    private final Sage plugin;

    public HealCommand(Sage plugin) {
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