package com.boes.sage.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class DisposeGUI {
    private final Player player;
    private final Inventory inventory;

    public DisposeGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§c§lDispose Items");
    }

    public void open() {
        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName("§c§lDispose");
        borderMeta.setLore(Arrays.asList(
            "§7Place items here to dispose of them.",
            "§7All items will be §c§lDELETED §7when you close this GUI!",
            "§c§lWARNING: This action cannot be undone!"
        ));
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }
        inventory.setItem(9, border);
        inventory.setItem(18, border);
        inventory.setItem(27, border);
        inventory.setItem(36, border);
        inventory.setItem(17, border);
        inventory.setItem(26, border);
        inventory.setItem(35, border);
        inventory.setItem(44, border);

        ItemStack info = new ItemStack(Material.BARRIER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§c§lDISPOSE ITEMS");
        infoMeta.setLore(Arrays.asList(
            "",
            "§7Place unwanted items in this GUI",
            "§7to permanently delete them.",
            "",
            "§c§lWARNING:",
            "§7All items will be §c§lDELETED",
            "§7when you close this inventory!",
            "",
            "§7This action §c§lCANNOT §7be undone!"
        ));
        info.setItemMeta(infoMeta);
        inventory.setItem(4, info);
    }

    public Inventory getInventory() {
        return inventory;
    }
}