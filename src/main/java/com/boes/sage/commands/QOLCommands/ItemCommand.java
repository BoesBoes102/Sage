package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class ItemCommand {

    private final Sage plugin;

    public ItemCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("item <material> [amount]")
    @Command("i <material> [amount]")
    @Permission("sage.item")
    public void onCommand(
            Player player,
            @Argument(value = "material", suggestions = "materials") Material material,
            @Argument(value = "amount", parserName = "positiveAmount") Integer amount
    ) {
        if (amount == null) {
            amount = 1;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be at least 1!");
            return;
        }

        int maxStackSize = material.getMaxStackSize();
        int emptySlots = 0;
        int partialStackSpace = 0;

        for (ItemStack invItem : player.getInventory().getStorageContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) {
                emptySlots++;
            } else if (invItem.getType() == material && invItem.getAmount() < maxStackSize) {
                partialStackSpace += (maxStackSize - invItem.getAmount());
            }
        }

        int maxCanFit = (emptySlots * maxStackSize) + partialStackSpace;
        int amountToGive = Math.min(amount, maxCanFit);

        if (amountToGive <= 0) {
            player.sendMessage("§cYour inventory does not have space for any " + formatMaterialName(material) + ".");
            return;
        }

        int remaining = amountToGive;
        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStackSize);
            player.getInventory().addItem(new ItemStack(material, stackAmount));
            remaining -= stackAmount;
        }

        player.sendMessage("§aGave you " + amountToGive + "x " + formatMaterialName(material) + "!");
    }

    private String formatMaterialName(Material material) {
        return material.name().toLowerCase().replace("_", " ");
    }
}
