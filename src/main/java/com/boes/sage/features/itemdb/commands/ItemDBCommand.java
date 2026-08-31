package com.boes.sage.features.itemdb.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.itemdb.ItemDatabaseService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.Set;

public class ItemDBCommand {
    private final Sage plugin;
    private final ItemDatabaseService itemDatabase;

    public ItemDBCommand(Sage plugin) {
        this.plugin = plugin;
        this.itemDatabase = plugin.getItemDatabaseService();
    }

    @Command("itemdb")
    @Permission("sage.itemdb")
    public void onDefault(Player player) {
        player.sendMessage("§cUsage: /itemdb <add|give|delete|list>");
    }

    @Command("itemdb add <name>")
    @Permission("sage.itemdb.add")
    public void onAdd(Player player, @Argument("name") String itemName) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cYou must be holding an item!");
            return;
        }

        String name = itemName.toLowerCase();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean added = itemDatabase.addItem(name, item);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!added) {
                        player.sendMessage("§cItem '" + name + "' already exists in the database!");
                        return;
                    }

                    player.sendMessage("§a§lItem '" + name + "' saved to database!");
                    player.sendMessage("§7Item: §f" + item.getType().toString());
                    player.sendMessage("§7Amount: §f" + item.getAmount());
                    if (item.hasItemMeta() && item.getItemMeta() != null) {
                        if (item.getItemMeta().hasDisplayName()) {
                            player.sendMessage("§7Display Name: §f" + item.getItemMeta().getDisplayName());
                        }
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cError: §c" + e.getMessage());
                    plugin.getLogger().warning("Failed to save item: " + e.getMessage());
                });
            }
        });
    }

    @Command("itemdb give <name>")
    @Permission("sage.itemdb.give")
    public void onGive(Player player, @Argument(value = "name", suggestions = "itemdb") String itemName) {
        String name = itemName.toLowerCase();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ItemStack item = itemDatabase.getItem(name);
                if (item == null) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage("§cItem '" + name + "' does not exist in the database!"));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), item);
                        player.sendMessage("§aItem '" + name + "' dropped on ground (inventory full)!");
                    } else {
                        player.getInventory().addItem(item);
                        player.sendMessage("§aItem '" + name + "' added to your inventory!");
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cError: §c" + e.getMessage());
                    plugin.getLogger().warning("Failed to give item: " + e.getMessage());
                });
            }
        });
    }

    @Command("itemdb delete <name>")
    @Permission("sage.itemdb.delete")
    public void onDelete(Player player, @Argument(value = "name", suggestions = "itemdb") String itemName) {
        String name = itemName.toLowerCase();

        if (!itemDatabase.itemExists(name)) {
            player.sendMessage("§cItem '" + name + "' does not exist in the database!");
            return;
        }

        itemDatabase.deleteItem(name);
        player.sendMessage("§a§lItem '" + name + "' deleted from database!");
    }

    @Command("itemdb list")
    @Permission("sage.itemdb.list")
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
