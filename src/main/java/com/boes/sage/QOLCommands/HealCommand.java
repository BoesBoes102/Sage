package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

@CommandAlias("heal")
@Description("Heal yourself or another player")
public class HealCommand extends BaseCommand {

    private final Sage plugin;

    public HealCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.heal")
    public void onCommand(Player sender, @Optional Player target) {
        if (target == null) {
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