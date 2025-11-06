package com.boes.sage.gui;

import com.boes.sage.Sage;
import com.boes.sage.managers.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class KitGUI {
    private final Player viewer;
    private final KitManager kitManager;
    private final Inventory inventory;

    public KitGUI(Sage plugin, Player viewer) {
        this.viewer = viewer;
        this.kitManager = plugin.getKitManager();
        this.inventory = Bukkit.createInventory(null, 54, "§8Available Kits");
    }

    public void open() {
        setupKits();
        viewer.openInventory(inventory);
    }

    private void setupKits() {
        Set<String> kitNames = kitManager.getKitNames();
        int slot = 10;

        for (String kitName : kitNames) {
            if (!viewer.hasPermission("sage.kit." + kitName)) {
                continue;
            }

            ItemStack kitItem = createKitButton(kitName);
            if (slot < 54) {
                inventory.setItem(slot, kitItem);
                slot++;
                if ((slot + 1) % 9 == 0) {
                    slot += 2;
                }
            }
        }

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private ItemStack createKitButton(String kitName) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§a§l" + kitName);

        List<String> lore = new ArrayList<>();
        String duration = kitManager.getKitDuration(kitName);
        
        if (duration.equals("0")) {
            lore.add("§7Duration: §bNo Cooldown");
        } else {
            lore.add("§7Duration: §b" + duration);
        }

        long cooldownRemaining = kitManager.getKitCooldownRemaining(viewer, kitName);
        if (cooldownRemaining > 0) {
            lore.add("§c§lNext claim: " + formatTime(cooldownRemaining));
        } else {
            lore.add("§a§lClick to claim!");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName("§8");
        glass.setItemMeta(meta);
        return glass;
    }

    private String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}