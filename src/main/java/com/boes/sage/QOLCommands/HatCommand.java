package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("hat")
@Description("Put the item in your hand on your head")
@Conditions("permission:sage.hat")
public class HatCommand extends BaseCommand {

    private final Sage plugin;

    public HatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    public void onCommand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item!");
            return;
        }

        ItemStack currentHelmet = player.getInventory().getHelmet();

        player.getInventory().setHelmet(itemInHand.clone());

        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            player.getInventory().setItemInMainHand(currentHelmet);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        player.sendMessage("§aYou are now wearing " + itemInHand.getType().name().toLowerCase().replace("_", " ") + " §aon your head!");
    }
}