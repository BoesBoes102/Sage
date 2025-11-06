package com.boes.sage.gui;

import com.boes.sage.Sage;
import com.boes.sage.data.PunishmentHistory;
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
        setupButtons();
        viewer.openInventory(inventory);
    }

    private void setupButtons() {
        inventory.setItem(1, createButton(Material.YELLOW_WOOL, "§e§lWARNINGS", "warn"));
        inventory.setItem(3, createButton(Material.RED_WOOL, "§c§lMUTES", "mute"));
        inventory.setItem(5, createButton(Material.BLACK_WOOL, "§4§lBANS", "ban"));
        inventory.setItem(7, createButton(Material.GRAY_WOOL, "§0§lBLACKLISTS", "blacklist"));
        inventory.setItem(10, createButton(Material.ORANGE_WOOL, "§6§lKICKS", "kick"));

        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private ItemStack createButton(Material material, String name, String type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        List<PunishmentHistory> history = plugin.getPunishmentManager().getHistoryByType(target.getUniqueId(), type);
        lore.add("§7Count: §f" + history.size());

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