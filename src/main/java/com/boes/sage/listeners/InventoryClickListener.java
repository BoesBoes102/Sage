package com.boes.sage.listeners;

import com.boes.sage.gui.HistoryDetailsGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.startsWith("§8Punishment History: ")) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) {
                return;
            }

            String displayName = meta.getDisplayName();
            String punishmentType = null;

            if (displayName.contains("WARNING")) punishmentType = "warn";
            else if (displayName.contains("MUTE")) punishmentType = "mute";
            else if (displayName.contains("BAN")) punishmentType = "ban";
            else if (displayName.contains("BLACKLIST")) punishmentType = "blacklist";
            else if (displayName.contains("KICK")) punishmentType = "kick";

            if (punishmentType != null) {
                String targetName = title.replace("§8Punishment History: §e", "");
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                new HistoryDetailsGUI((com.boes.sage.Sage) Bukkit.getPluginManager().getPlugin("Sage"), player, target, punishmentType).open();
            }
            return;
        }

        if (title.contains("§8") && title.contains("S: §e")) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) {
                return;
            }

            if (meta.getDisplayName().contains("Back")) {
                String targetName = title.split("§e")[1];
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                new com.boes.sage.gui.HistoryGUI((com.boes.sage.Sage) Bukkit.getPluginManager().getPlugin("Sage"), player, target).open();
            }
            return;
        }

        if (title.equals("§8Available Kits")) {
            event.setCancelled(true);
            
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                return;
            }
            
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) {
                return;
            }
            
            String kitName = meta.getDisplayName().replace("§a§l", "");
            
            player.performCommand("kit claim " + kitName);
            player.closeInventory();
        }

        if (title.equals("§c§lDispose Items")) {
            int slot = event.getRawSlot();
            
            if (slot < 54) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                    event.setCancelled(true);
                }
                if (clickedItem != null && clickedItem.getType() == Material.BARRIER) {
                    event.setCancelled(true);
                }
            }
        }

        if (title.equals("§6§lSelect World")) {
            event.setCancelled(true);
            
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) {
                return;
            }
            
            String worldName = meta.getDisplayName().replace("§e§l", "");
            World world = Bukkit.getWorld(worldName);
            
            if (world != null) {
                int highestY = world.getHighestBlockYAt(0, 0) + 1;
                Location teleportLocation = new Location(world, 0.5, highestY, 0.5);
                
                player.closeInventory();
                player.teleport(teleportLocation);
                player.sendMessage("§aTeleported to §e" + worldName + " §aat coordinates §e0, " + highestY + ", 0");
            } else {
                player.sendMessage("§cWorld not found!");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        if (title.equals("§c§lDispose Items")) {
            event.getInventory().clear();
        }
    }
}