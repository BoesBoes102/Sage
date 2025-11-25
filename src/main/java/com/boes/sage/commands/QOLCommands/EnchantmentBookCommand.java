package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

@CommandAlias("enchantmentbook")
@Description("Create enchanted books")
@CommandPermission("sage.enchantmentbook")
public class EnchantmentBookCommand extends BaseCommand {

    private final Sage plugin;

    public EnchantmentBookCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @CommandCompletion("@enchantments 1|2|3|4|5 1|10|64")
    public void onCommand(Player player, Enchantment enchantment, int level, int amount) {
        if (level < 1) {
            player.sendMessage("§cLevel must be at least 1!");
            return;
        }

        if (amount < 1) {
            player.sendMessage("§cAmount must be at least 1!");
            return;
        }

        int maxStackSize = Material.ENCHANTED_BOOK.getMaxStackSize();
        int emptySlots = 0;
        int partialStackSpace = 0;

        for (ItemStack invItem : player.getInventory().getStorageContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) {
                emptySlots++;
            } else if (invItem.getType() == Material.ENCHANTED_BOOK && invItem.getAmount() < maxStackSize) {
                partialStackSpace += (maxStackSize - invItem.getAmount());
            }
        }

        int maxCanFit = (emptySlots * maxStackSize) + partialStackSpace;

        if (amount > maxCanFit) {
            player.sendMessage("§cCannot give " + amount + " books! Only " + maxCanFit + " can fit in your inventory.");
            return;
        }

        int remaining = amount;
        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStackSize);
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, stackAmount);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

            meta.addStoredEnchant(enchantment, level, true);

            book.setItemMeta(meta);
            player.getInventory().addItem(book);
            remaining -= stackAmount;
        }

        player.sendMessage("§aGave you §e" + amount + "x §b" + formatEnchantmentName(enchantment.getKey().getKey()) + " " + level + "book(s)");
    }

    private String formatEnchantmentName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }
}