package com.boes.sage.gui;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DisposeGUI implements Listener {
    public static final String TITLE = "\u00A7c\u00A7lDispose Items";
    private static final Set<Integer> PROTECTED_SLOTS = new HashSet<>(Arrays.asList(
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 17, 18, 26, 27, 35, 36, 44,
        45, 46, 47, 48, 49, 50, 51, 52, 53
    ));

    private final Player player;
    private final Inventory inventory;

    public DisposeGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, TITLE);
    }

    public void open() {
        setupGUI();
        Bukkit.getPluginManager().registerEvents(this, Sage.getInstance());
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTopInventory().equals(inventory)) {
            return;
        }

        if (event.getAction().name().equals("COLLECT_TO_CURSOR")) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        if (PROTECTED_SLOTS.contains(event.getRawSlot())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getView().getTopInventory().equals(inventory)) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        boolean draggingIntoTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
        if (!draggingIntoTop) {
            return;
        }

        ItemStack oldCursor = event.getOldCursor();
        if (oldCursor == null || oldCursor.getType() == Material.AIR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        inventory.clear();
        HandlerList.unregisterAll(this);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
