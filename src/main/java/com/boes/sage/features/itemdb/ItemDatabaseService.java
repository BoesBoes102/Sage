package com.boes.sage.features.itemdb;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
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
import java.util.HashSet;
import java.util.Set;

public class ItemDatabaseService {
    private final DatabaseManager databaseManager;

    public ItemDatabaseService(Sage plugin) {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public boolean addItem(String name, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            throw new IllegalArgumentException("Cannot save air or null item!");
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT OR IGNORE INTO item_db (name, timestamp, serialized) VALUES (?, ?, ?)")) {
            statement.setString(1, name.toLowerCase());
            statement.setLong(2, System.currentTimeMillis());
            statement.setString(3, serializeItemStack(item));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save item " + name, e);
        }
    }

    public ItemStack getItem(String name) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT serialized FROM item_db WHERE name = ?")) {
            statement.setString(1, name.toLowerCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return deserializeItemStack(resultSet.getString("serialized"));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteItem(String name) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM item_db WHERE name = ?")) {
            statement.setString(1, name.toLowerCase());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item " + name, e);
        }
    }

    public boolean itemExists(String name) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM item_db WHERE name = ?")) {
            statement.setString(1, name.toLowerCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check item existence for " + name, e);
        }
    }

    public Set<String> getItemNames() {
        Set<String> names = new HashSet<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name FROM item_db");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load item names", e);
        }
        return names;
    }

    private String serializeItemStack(ItemStack item) {
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

    private ItemStack deserializeItemStack(String serialized) {
        try {
            byte[] decoded = Base64.getMimeDecoder().decode(serialized);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize item", e);
        }
    }
}
