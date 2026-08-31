package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class HatCommand {

    private final Sage plugin;

    public HatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("hat")
    @Permission("sage.hat")
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
