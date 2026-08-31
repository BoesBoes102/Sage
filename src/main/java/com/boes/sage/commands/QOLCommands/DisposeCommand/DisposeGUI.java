package com.boes.sage.commands.QOLCommands.DisposeCommand;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class DisposeGUI {
    public static final String TITLE = "\u00A7c\u00A7lDispose Items";

    private final Player player;
    private final Inventory inventory;

    public DisposeGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, TITLE);
    }

    public void open() {
        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName("\u00A7c\u00A7lDispose");
        borderMeta.setLore(Arrays.asList(
            "\u00A77Place items here to dispose of them.",
            "\u00A77All items will be \u00A7c\u00A7lDELETED \u00A77when you close this GUI!",
            "\u00A7c\u00A7lWARNING: This action cannot be undone!"
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
        infoMeta.setDisplayName("\u00A7c\u00A7lDISPOSE ITEMS");
        infoMeta.setLore(Arrays.asList(
            "",
            "\u00A77Place unwanted items in this GUI",
            "\u00A77to permanently delete them.",
            "",
            "\u00A7c\u00A7lWARNING:",
            "\u00A77All items will be \u00A7c\u00A7lDELETED",
            "\u00A77when you close this inventory!",
            "",
            "\u00A77This action \u00A7c\u00A7lCANNOT \u00A77be undone!"
        ));
        info.setItemMeta(infoMeta);
        inventory.setItem(4, info);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
