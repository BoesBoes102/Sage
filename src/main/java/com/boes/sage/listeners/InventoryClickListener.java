package com.boes.sage.listeners;

import com.boes.sage.gui.DisposeGUI;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(DisposeGUI.TITLE)) {
            handleDisposeClick(event);
        }
    }

    private void handleDisposeClick(InventoryClickEvent event) {
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

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(DisposeGUI.TITLE)) {
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
        if (event.getView().getTitle().equals(DisposeGUI.TITLE)) {
            event.getInventory().clear();
        }
    }
}
