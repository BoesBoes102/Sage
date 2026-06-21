package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("item|i")
@Description("Give yourself an item")
@CommandPermission("sage.item")
public class ItemCommand extends BaseCommand {

    private final Sage plugin;

    public ItemCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<material> [amount]")
    @CommandCompletion("@materials 1|2|3|4|5|6|7|8|9")
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
