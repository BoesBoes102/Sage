package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.boes.sage.Sage;
import com.boes.sage.managers.ItemDatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

@CommandAlias("itemdb")
public class ItemDBCommand extends BaseCommand {
    private final Sage plugin;
    private final ItemDatabaseManager itemDatabase;

    public ItemDBCommand(Sage plugin) {
        this.plugin = plugin;
        this.itemDatabase = plugin.getItemDatabaseManager();
    }

    @Default
    public void onDefault(Player player) {
        player.sendMessage("§cUsage: /itemdb <add|give|delete|list>");
    }

    @Subcommand("add")
    @Conditions("player-only")
    @CommandPermission("sage.itemdb.add")
    public void onAdd(Player player, String itemName) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cYou must be holding an item!");
            return;
        }

        String name = itemName.toLowerCase();

        if (itemDatabase.itemExists(name)) {
            player.sendMessage("§cItem '" + name + "' already exists in the database!");
            return;
        }

        try {
            itemDatabase.addItem(name, item);
            player.sendMessage("§a§lItem '" + name + "' saved to database!");
            player.sendMessage("§7Item: §f" + item.getType().toString());
            player.sendMessage("§7Amount: §f" + item.getAmount());
            if (item.hasItemMeta() && item.getItemMeta() != null) {
                if (item.getItemMeta().hasDisplayName()) {
                    player.sendMessage("§7Display Name: §f" + item.getItemMeta().getDisplayName());
                }
            }
        } catch (Exception e) {
            player.sendMessage("§c§lError: §c" + e.getMessage());
            plugin.getLogger().warning("Failed to save item: " + e.getMessage());
        }
    }

    @Subcommand("give")
    @Conditions("player-only")
    @CommandPermission("sage.itemdb.give")
    @CommandCompletion("@itemdb")
    public void onGive(Player player, String itemName) {
        String name = itemName.toLowerCase();

        if (!itemDatabase.itemExists(name)) {
            player.sendMessage("§cItem '" + name + "' does not exist in the database!");
            return;
        }

        try {
            ItemStack item = itemDatabase.getItem(name);
            if (item == null) {
                player.sendMessage("§cFailed to retrieve item!");
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
                player.sendMessage("§a§lItem '" + name + "' dropped on ground (inventory full)!");
            } else {
                player.getInventory().addItem(item);
                player.sendMessage("§a§lItem '" + name + "' added to your inventory!");
            }
        } catch (Exception e) {
            player.sendMessage("§c§lError: §c" + e.getMessage());
            plugin.getLogger().warning("Failed to give item: " + e.getMessage());
        }
    }

    @Subcommand("delete")
    @CommandPermission("sage.itemdb.delete")
    @CommandCompletion("@itemdb")
    public void onDelete(Player player, String itemName) {
        String name = itemName.toLowerCase();

        if (!itemDatabase.itemExists(name)) {
            player.sendMessage("§cItem '" + name + "' does not exist in the database!");
            return;
        }

        itemDatabase.deleteItem(name);
        player.sendMessage("§a§lItem '" + name + "' deleted from database!");
    }

    @Subcommand("list")
    @CommandPermission("sage.itemdb.list")
    public void onList(Player player) {
        Set<String> items = itemDatabase.getItemNames();

        if (items.isEmpty()) {
            player.sendMessage("§7No items in database.");
            return;
        }

        player.sendMessage("§a§l=== Item Database ===");
        player.sendMessage("§7Total items: §f" + items.size());
        player.sendMessage("§7Items:");

        items.stream()
                .sorted()
                .forEach(name -> player.sendMessage("  §f• " + name));
    }
}