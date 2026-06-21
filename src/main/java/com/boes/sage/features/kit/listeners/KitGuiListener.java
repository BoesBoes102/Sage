package com.boes.sage.features.kit.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitGuiListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Â§8Available Kits")) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR
                || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) {
            return;
        }

        String kitName = meta.getDisplayName().replace("Â§aÂ§l", "");
        player.performCommand("kit claim " + kitName);
        player.closeInventory();
    }
}
