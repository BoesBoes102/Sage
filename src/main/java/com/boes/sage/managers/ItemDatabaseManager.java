package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ItemDatabaseManager {
    private final JsonStorageManager storageManager;

    public ItemDatabaseManager(Sage plugin) {
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "itemdb.json"));
    }

    public void addItem(String name, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            throw new IllegalArgumentException("Cannot save air or null item!");
        }

        JsonObject json = storageManager.load();
        JsonObject itemObj = new JsonObject();
        itemObj.addProperty("timestamp", System.currentTimeMillis());
        itemObj.addProperty("serialized", serializeItemStack(item));
        
        json.add(name.toLowerCase(), itemObj);
        storageManager.save(json);
    }

    public ItemStack getItem(String name) {
        JsonObject json = storageManager.load();
        if (!json.has(name.toLowerCase())) {
            return null;
        }

        try {
            JsonObject itemObj = json.getAsJsonObject(name.toLowerCase());
            String serialized = itemObj.get("serialized").getAsString();
            return deserializeItemStack(serialized);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteItem(String name) {
        JsonObject json = storageManager.load();
        json.remove(name.toLowerCase());
        storageManager.save(json);
    }

    public boolean itemExists(String name) {
        return storageManager.load().has(name.toLowerCase());
    }

    public Set<String> getItemNames() {
        JsonObject json = storageManager.load();
        Set<String> names = new HashSet<>();
        for (String key : json.keySet()) {
            names.add(key);
        }
        return names;
    }

    private String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize item", e);
        }
    }

    private ItemStack deserializeItemStack(String serialized) {
        try {
            byte[] decoded = Base64Coder.decodeLines(serialized);
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