package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("item")
@Description("Give yourself an item")
public class ItemCommand extends BaseCommand {

    private final Sage plugin;

    public ItemCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Conditions("permission:sage.item")
    public void onCommand(Player player, Material material, @Optional Integer amount) {
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

        if (amount > maxCanFit) {
            player.sendMessage("§cCannot give " + amount + " items! Only " + maxCanFit + " can fit in your inventory.");
            return;
        }

        int remaining = amount;
        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStackSize);
            ItemStack item = new ItemStack(material, stackAmount);
            player.getInventory().addItem(item);
            remaining -= stackAmount;
        }

        player.sendMessage("§aGave you " + amount + "x " + material.name().toLowerCase().replace("_", " ") + "!");
    }
}