package com.boes.sage.Utils;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflinePlayerDataManager {
    private static final Map<UUID, OfflinePlayerInventoryData> editedInventories = new HashMap<>();
    private static DatabaseManager databaseManager;

    public static void init(Sage plugin) {
        databaseManager = plugin.getDatabaseManager();
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

    public static void saveOfflinePlayerData(UUID uuid, Inventory inventory, String type) {
        if (!editedInventories.containsKey(uuid)) {
            return;
        }

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            if (type.equals("ENDER_CHEST")) {
                onlinePlayer.getEnderChest().setContents(inventory.getContents());
            }
            return;
        }

        try {
            if (type.equals("ENDER_CHEST")) {
                saveEnderChestToFile(uuid, inventory.getContents());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncOnPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        OfflinePlayerInventoryData data = editedInventories.get(uuid);
        if (data == null) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Sage.getInstance(), () -> {
            try {
                if (data.type.equals("ENDER_CHEST")) {
                    ItemStack[] enderItems = loadEnderChestFromFile(uuid);
                    Bukkit.getScheduler().runTask(Sage.getInstance(), () -> {
                        if (player.isOnline()) {
                            player.getEnderChest().setContents(enderItems);
                        }
                        editedInventories.remove(uuid);
                    });
                } else if (data.type.equals("INVENTORY")) {
                    ItemStack[] invItems = loadInventoryFromFile(uuid);
                    ItemStack[] armor = loadArmorFromFile(uuid);
                    ItemStack offhand = loadOffhandFromFile(uuid);

                    Bukkit.getScheduler().runTask(Sage.getInstance(), () -> {
                        if (player.isOnline()) {
                            player.getInventory().setContents(invItems);
                            player.getInventory().setArmorContents(armor);
                            player.getInventory().setItemInOffHand(offhand);
                        }
                        editedInventories.remove(uuid);
                    });
                } else {
                    editedInventories.remove(uuid);
                }
            } catch (Exception e) {
                e.printStackTrace();
                editedInventories.remove(uuid);
            }
        });
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

    public static ItemStack[] loadOfflineEnderChest(UUID uuid) throws Exception {
        return loadEnderChestFromFile(uuid);
    }

    private static ItemStack[] loadEnderChestFromFile(UUID uuid) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT serialized FROM offline_inventory_enderchest WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || resultSet.getString("serialized") == null) {
                    return new ItemStack[27];
                }
                return deserializeItemStackArray(resultSet.getString("serialized"));
            }
        }
    }

    public static ItemStack[] loadInventoryFromFile(UUID uuid) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT items FROM offline_inventory_data WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || resultSet.getString("items") == null) {
                    return new ItemStack[36];
                }
                return deserializeItemStackArray(resultSet.getString("items"));
            }
        }
    }

    public static ItemStack[] loadArmorFromFile(UUID uuid) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT armor FROM offline_inventory_data WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || resultSet.getString("armor") == null) {
                    return new ItemStack[4];
                }
                return deserializeItemStackArray(resultSet.getString("armor"));
            }
        }
    }

    public static ItemStack loadOffhandFromFile(UUID uuid) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT offhand FROM offline_inventory_data WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || resultSet.getString("offhand") == null) {
                    return null;
                }
                return deserializeItemStack(resultSet.getString("offhand"));
            }
        }
    }

    private static void saveEnderChestToFile(UUID uuid, ItemStack[] items) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO offline_inventory_enderchest (uuid, serialized) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET serialized = excluded.serialized")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serializeItemStackArray(items));
            statement.executeUpdate();
        }
    }

    public static void saveOfflineEnderChest(UUID uuid, ItemStack[] items) throws Exception {
        saveEnderChestToFile(uuid, items);

        // Mark as edited
        if (!editedInventories.containsKey(uuid)) {
            editedInventories.put(uuid, new OfflinePlayerInventoryData(null, uuid, "ENDER_CHEST"));
        }
    }

    public static void saveInventoryToFile(UUID uuid, ItemStack[] items) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO offline_inventory_data (uuid, items) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET items = excluded.items")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serializeItemStackArray(items));
            statement.executeUpdate();
        }

        // Mark as edited
        if (!editedInventories.containsKey(uuid)) {
            editedInventories.put(uuid, new OfflinePlayerInventoryData(null, uuid, "INVENTORY"));
        }
    }

    public static void saveArmorToFile(UUID uuid, ItemStack[] armor) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO offline_inventory_data (uuid, armor) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET armor = excluded.armor")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serializeItemStackArray(armor));
            statement.executeUpdate();
        }

        // Mark as edited
        if (!editedInventories.containsKey(uuid)) {
            editedInventories.put(uuid, new OfflinePlayerInventoryData(null, uuid, "INVENTORY"));
        }
    }

    public static void saveOffhandToFile(UUID uuid, ItemStack offhand) throws Exception {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO offline_inventory_data (uuid, offhand) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET offhand = excluded.offhand")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, offhand == null ? null : serializeItemStack(offhand));
            statement.executeUpdate();
        }

        // Mark as edited
        if (!editedInventories.containsKey(uuid)) {
            editedInventories.put(uuid, new OfflinePlayerInventoryData(null, uuid, "INVENTORY"));
        }
    }

    private static String serializeItemStackArray(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(items);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }
    }

    private static ItemStack[] deserializeItemStackArray(String serialized) throws Exception {
        byte[] decoded = Base64.getMimeDecoder().decode(serialized);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack[] items = (ItemStack[]) dataInput.readObject();
        dataInput.close();
        return items;
    }

    private static String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize item", e);
        }
    }

    private static ItemStack deserializeItemStack(String serialized) throws Exception {
        byte[] decoded = Base64.getMimeDecoder().decode(serialized);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
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
