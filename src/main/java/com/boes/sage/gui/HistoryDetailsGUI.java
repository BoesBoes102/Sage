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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryDetailsGUI {
    private final Sage plugin;
    private final Player viewer;
    private final OfflinePlayer target;
    private final String punishmentType;
    private final Inventory inventory;

    public HistoryDetailsGUI(Sage plugin, Player viewer, OfflinePlayer target, String punishmentType) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.punishmentType = punishmentType;
        this.inventory = Bukkit.createInventory(null, 54, "§8" + punishmentType.toUpperCase() + "S: §e" + target.getName());
    }

    public void open() {
        setupButtons();
        viewer.openInventory(inventory);
    }

    private void setupButtons() {
        List<PunishmentHistory> history = plugin.getPunishmentManager().getHistoryByType(target.getUniqueId(), punishmentType);

        int slot = 10;
        for (PunishmentHistory h : history) {
            if (slot >= 54) break;

            Material woolColor = getWoolColor(punishmentType);
            ItemStack punishmentItem = new ItemStack(woolColor);
            ItemMeta meta = punishmentItem.getItemMeta();

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            meta.setDisplayName("§e" + punishmentType.substring(0, 1).toUpperCase() + punishmentType.substring(1));

            List<String> lore = new ArrayList<>();
            lore.add("§7Reason: §f" + h.getReason());
            lore.add("§7Punisher: §f" + h.getPunisher());
            lore.add("§7Date: §f" + sdf.format(new Date(h.getTimestamp())));
            if (h.getDuration() != null && !h.getDuration().isEmpty()) {
                lore.add("§7Duration: §f" + h.getDuration());
            }

            meta.setLore(lore);
            punishmentItem.setItemMeta(meta);
            inventory.setItem(slot, punishmentItem);

            slot++;
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c← Back");
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createGlassPane());
            }
        }
    }

    private Material getWoolColor(String type) {
        return switch (type.toLowerCase()) {
            case "warn" -> Material.YELLOW_WOOL;
            case "mute" -> Material.RED_WOOL;
            case "ban" -> Material.BLACK_WOOL;
            case "blacklist" -> Material.GRAY_WOOL;
            case "kick" -> Material.ORANGE_WOOL;
            default -> Material.WHITE_WOOL;
        };
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}