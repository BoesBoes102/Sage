package com.boes.sage.features.punishment.gui;

import com.boes.sage.Sage;
import com.boes.sage.features.punishment.data.PunishmentHistory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HistoryGUI {
    private final Sage plugin;
    private final Player viewer;
    private final OfflinePlayer target;
    private final Inventory inventory;

    public HistoryGUI(Sage plugin, Player viewer, OfflinePlayer target) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.inventory = Bukkit.createInventory(null, 27, "§8Punishment History: §e" + target.getName());
    }

    public void open() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<PunishmentHistory> fullHistory = plugin.getPunishmentService().getPlayerHistory(target.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                setupButtons(fullHistory);
                if (viewer.isOnline()) {
                    viewer.openInventory(inventory);
                }
            });
        });
    }

    private void setupButtons(List<PunishmentHistory> fullHistory) {
        inventory.setItem(11, createButton(fullHistory, Material.YELLOW_WOOL, "§e§lWARNINGS", "warn"));
        inventory.setItem(12, createButton(fullHistory, Material.RED_WOOL, "§c§lMUTES", "mute"));
        inventory.setItem(13, createButton(fullHistory, Material.BLACK_WOOL, "§4§lBANS", "ban"));
        inventory.setItem(14, createButton(fullHistory, Material.GRAY_WOOL, "§0§lBLACKLISTS", "blacklist"));
        inventory.setItem(15, createButton(fullHistory, Material.ORANGE_WOOL, "§6§lKICKS", "kick"));

        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private ItemStack createButton(List<PunishmentHistory> fullHistory, Material material, String name, String type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        long count = fullHistory.stream().filter(h -> h.getType().equalsIgnoreCase(type)).count();
        List<String> lore = new ArrayList<>();
        lore.add("§7Count: §f" + count);

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}