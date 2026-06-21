package com.boes.sage.features.punishment.listeners;

import com.boes.sage.Sage;
import com.boes.sage.features.punishment.gui.HistoryDetailsGUI;
import com.boes.sage.features.punishment.gui.HistoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PunishmentHistoryListener implements Listener {
    private final Sage plugin;

    public PunishmentHistoryListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.startsWith("Â§8Punishment History: ")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR
                    || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
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
                String targetName = title.replace("Â§8Punishment History: Â§e", "");
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                new HistoryDetailsGUI(plugin, player, target, punishmentType).open();
            }
            return;
        }

        if (!title.contains("Â§8") || !title.contains("S: Â§e")) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null || !meta.getDisplayName().contains("Back")) {
            return;
        }

        String targetName = title.split("Â§e")[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        new HistoryGUI(plugin, player, target).open();
    }
}
