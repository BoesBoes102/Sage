package com.boes.sage.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class OfflinePlayerDataManager {
    private static final Map<UUID, OfflinePlayerInventoryData> editedInventories = new HashMap<>();
    private static final File DATA_FOLDER = new File(Bukkit.getServer().getWorldContainer(), "offline-inventory-data");

    static {
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs();
        }
    }

    public static Inventory getOfflinePlayerEnderChest(String playerName) {
        UUID uuid = getPlayerUUID(playerName);
        if (uuid == null) {
            return null;
        }

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            Inventory chest = Bukkit.createInventory(null, 27, "§8" + playerName + "'s Ender Chest");
            chest.setContents(onlinePlayer.getEnderChest().getContents());
            return chest;
        }

        try {
            ItemStack[] enderChestItems = loadEnderChestFromFile(uuid);
            Inventory chest = Bukkit.createInventory(null, 27, "§8" + playerName + "'s Ender Chest");
            chest.setContents(enderChestItems);

            editedInventories.put(uuid, new OfflinePlayerInventoryData(playerName, uuid, "ENDER_CHEST"));
            
            return chest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Inventory getOfflinePlayerInventory(String playerName) {
        UUID uuid = getPlayerUUID(playerName);
        if (uuid == null) {
            return null;
        }

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            Inventory inv = Bukkit.createInventory(null, 36, "§8" + playerName + "'s Inventory");
            inv.setContents(onlinePlayer.getInventory().getContents());
            return inv;
        }

        try {
            ItemStack[] inventoryItems = loadInventoryFromFile(uuid);
            Inventory inv = Bukkit.createInventory(null, 36, "§8" + playerName + "'s Inventory");
            inv.setContents(inventoryItems);

            editedInventories.put(uuid, new OfflinePlayerInventoryData(playerName, uuid, "INVENTORY"));
            
            return inv;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveOfflinePlayerData(UUID uuid, Inventory inventory, String type) {
        if (!editedInventories.containsKey(uuid)) {
            return;
        }

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            if (type.equals("ENDER_CHEST")) {
                onlinePlayer.getEnderChest().setContents(inventory.getContents());
            } else if (type.equals("INVENTORY")) {
                onlinePlayer.getInventory().setContents(inventory.getContents());
            }
            return;
        }

        try {
            if (type.equals("ENDER_CHEST")) {
                saveEnderChestToFile(uuid, inventory.getContents());
            } else if (type.equals("INVENTORY")) {
                saveInventoryToFile(uuid, inventory.getContents());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncOnPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        if (editedInventories.containsKey(uuid)) {
            OfflinePlayerInventoryData data = editedInventories.get(uuid);
            
            try {
                if (data.type.equals("ENDER_CHEST")) {
                    ItemStack[] enderItems = loadEnderChestFromFile(uuid);
                    player.getEnderChest().setContents(enderItems);
                } else if (data.type.equals("INVENTORY")) {
                    ItemStack[] invItems = loadInventoryFromFile(uuid);
                    player.getInventory().setContents(invItems);
                }
                editedInventories.remove(uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clearCache(UUID uuid) {
        editedInventories.remove(uuid);
    }

    public static UUID getPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        try {
            var offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.getUniqueId() != null) {
                return offlinePlayer.getUniqueId();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static ItemStack[] loadEnderChestFromFile(UUID uuid) throws Exception {
        File file = getEnderChestFile(uuid);
        if (!file.exists()) {
            return new ItemStack[27];
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack[] items = new ItemStack[27];

        for (int i = 0; i < 27; i++) {
            if (config.contains("items." + i)) {
                items[i] = config.getItemStack("items." + i);
            }
        }

        return items;
    }

    private static ItemStack[] loadInventoryFromFile(UUID uuid) throws Exception {
        File file = getInventoryFile(uuid);
        if (!file.exists()) {
            return new ItemStack[36];
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack[] items = new ItemStack[36];

        for (int i = 0; i < 36; i++) {
            if (config.contains("items." + i)) {
                items[i] = config.getItemStack("items." + i);
            }
        }

        return items;
    }

    public static ItemStack[] loadArmorFromFile(UUID uuid) throws Exception {
        File file = getInventoryFile(uuid);
        if (!file.exists()) {
            return new ItemStack[4];
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack[] armor = new ItemStack[4];

        for (int i = 0; i < 4; i++) {
            if (config.contains("armor." + i)) {
                armor[i] = config.getItemStack("armor." + i);
            }
        }

        return armor;
    }

    public static ItemStack loadOffhandFromFile(UUID uuid) throws Exception {
        File file = getInventoryFile(uuid);
        if (!file.exists()) {
            return null;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getItemStack("offhand");
    }

    private static void saveEnderChestToFile(UUID uuid, ItemStack[] items) throws Exception {
        File file = getEnderChestFile(uuid);
        FileConfiguration config = new YamlConfiguration();

        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                config.set("items." + i, items[i]);
            }
        }

        config.save(file);
    }

    private static void saveInventoryToFile(UUID uuid, ItemStack[] items) throws Exception {
        File file = getInventoryFile(uuid);
        FileConfiguration config = new YamlConfiguration();

        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() != Material.AIR) {
                config.set("items." + i, items[i]);
            }
        }

        config.save(file);
    }

    public static void saveArmorToFile(UUID uuid, ItemStack[] armor) throws Exception {
        File file = getInventoryFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            config = new YamlConfiguration();
        }

        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && armor[i].getType() != Material.AIR) {
                config.set("armor." + i, armor[i]);
            }
        }

        config.save(file);
    }

    public static void saveOffhandToFile(UUID uuid, ItemStack offhand) throws Exception {
        File file = getInventoryFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            config = new YamlConfiguration();
        }

        if (offhand != null && offhand.getType() != Material.AIR) {
            config.set("offhand", offhand);
        }

        config.save(file);
    }

    private static File getEnderChestFile(UUID uuid) {
        return new File(DATA_FOLDER, uuid + "-ender-chest.yml");
    }

    private static File getInventoryFile(UUID uuid) {
        return new File(DATA_FOLDER, uuid + "-inventory.yml");
    }

    private static class OfflinePlayerInventoryData {
        String playerName;
        UUID uuid;
        String type;

        OfflinePlayerInventoryData(String playerName, UUID uuid, String type) {
            this.playerName = playerName;
            this.uuid = uuid;
            this.type = type;
        }
    }
}