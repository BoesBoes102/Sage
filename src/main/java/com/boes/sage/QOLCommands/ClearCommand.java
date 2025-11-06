package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("clear")
@Description("Clear your inventory or another player's inventory")
public class ClearCommand extends BaseCommand {

    private final Sage plugin;

    public ClearCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.clear")
    public void onCommand(Player sender, @Optional Player target) {
        if (target == null) {
            target = sender;
        }

        target.getInventory().clear();

        target.getInventory().setHelmet(null);
        target.getInventory().setChestplate(null);
        target.getInventory().setLeggings(null);
        target.getInventory().setBoots(null);

        target.getInventory().setItemInOffHand(new ItemStack(Material.AIR));

        if (sender.equals(target)) {
            target.sendMessage("§aYour inventory has been cleared!");
        } else {
            target.sendMessage("§cYour inventory has been cleared by " + sender.getName() + "!");
            sender.sendMessage("§aCleared " + target.getName() + "'s inventory!");
        }
    }
}