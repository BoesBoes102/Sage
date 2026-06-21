package com.boes.sage.features.refund;

import com.boes.sage.Sage;
import com.boes.sage.features.refund.data.RefundSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class RefundService {
    private static final long REFUND_LIFETIME_MILLIS = 3L * 24L * 60L * 60L * 1000L;

    private final Sage plugin;
    private final File dataFolder;

    public RefundService(Sage plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "refunds");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        cleanupExpiredRefunds();
        Bukkit.getScheduler().runTaskTimer(plugin, (Runnable) this::cleanupExpiredRefunds, 20L * 60L * 30L, 20L * 60L * 30L);
    }

    public RefundSnapshot saveSnapshot(Player player, String saveReason, String deathReason, Location location) {
        long createdAt = System.currentTimeMillis();
        String id = createdAt + "-" + UUID.randomUUID().toString().substring(0, 8);
        RefundSnapshot snapshot = new RefundSnapshot(
                id,
                player.getUniqueId(),
                player.getName(),
                createdAt,
                createdAt + REFUND_LIFETIME_MILLIS,
                saveReason,
                deathReason,
                location == null ? player.getLocation().clone() : location.clone(),
                cloneItems(player.getInventory().getStorageContents(), 36),
                cloneItems(player.getInventory().getArmorContents(), 4),
                cloneItem(player.getInventory().getItemInOffHand()),
                Math.max(0, player.getTotalExperience()),
                Math.max(0, player.getLevel()),
                Math.max(0, player.getExp()),
                false
        );

        saveSnapshot(snapshot);
        return snapshot;
    }

    public List<RefundSnapshot> getRefunds(UUID playerUuid) {
        cleanupExpiredRefunds(playerUuid);
        FileConfiguration config = loadConfig(playerUuid);
        List<RefundSnapshot> snapshots = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("refunds");
        if (section == null) {
            return snapshots;
        }

        for (String id : section.getKeys(false)) {
            RefundSnapshot snapshot = loadSnapshot(config, playerUuid, id);
            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }

        snapshots.sort(Comparator.comparingLong(RefundSnapshot::getCreatedAt).reversed());
        return snapshots;
    }

    public List<RefundSnapshot> getPendingRefunds(UUID playerUuid) {
        List<RefundSnapshot> pending = new ArrayList<>();
        for (RefundSnapshot snapshot : getRefunds(playerUuid)) {
            if (snapshot.isPendingClaim()) {
                pending.add(snapshot);
            }
        }
        return pending;
    }

    public RefundSnapshot getRefund(UUID playerUuid, String id) {
        cleanupExpiredRefunds(playerUuid);
        return loadSnapshot(loadConfig(playerUuid), playerUuid, id);
    }

    public void markPending(RefundSnapshot snapshot, boolean pending) {
        FileConfiguration config = loadConfig(snapshot.getPlayerUuid());
        config.set(path(snapshot.getId(), "pendingClaim"), pending);
        saveConfig(snapshot.getPlayerUuid(), config);
    }

    public void deleteRefund(UUID playerUuid, String id) {
        FileConfiguration config = loadConfig(playerUuid);
        config.set("refunds." + id, null);
        saveConfig(playerUuid, config);
    }

    public void applyAdditiveRefund(Player player, RefundSnapshot snapshot) {
        giveItems(player, snapshot);
        if (snapshot.getTotalExperience() > 0) {
            player.giveExp(snapshot.getTotalExperience());
        }
        markPending(snapshot, false);
    }

    public void applyOverrideRefund(Player player, RefundSnapshot snapshot) {
        player.getInventory().setStorageContents(cloneItems(snapshot.getInventory(), 36));
        player.getInventory().setArmorContents(cloneItems(snapshot.getArmor(), 4));
        player.getInventory().setItemInOffHand(cloneItem(snapshot.getOffhand()));
        setTotalExperience(player, snapshot.getTotalExperience());
        player.updateInventory();
    }

    public void teleportToSavedLocation(Player player, RefundSnapshot snapshot) {
        Location location = snapshot.getLocation();
        if (location != null && location.getWorld() != null) {
            player.teleport(location);
        }
    }

    public void cleanupExpiredRefunds() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                cleanupExpiredRefunds(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void cleanupExpiredRefunds(UUID playerUuid) {
        FileConfiguration config = loadConfig(playerUuid);
        ConfigurationSection section = config.getConfigurationSection("refunds");
        if (section == null) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean changed = false;
        for (String id : new ArrayList<>(section.getKeys(false))) {
            long expiresAt = config.getLong(path(id, "expiresAt"), 0);
            if (expiresAt > 0 && expiresAt <= now) {
                config.set("refunds." + id, null);
                changed = true;
            }
        }

        if (changed) {
            saveConfig(playerUuid, config);
        }
    }

    private void giveItems(Player player, RefundSnapshot snapshot) {
        for (ItemStack item : snapshot.getInventory()) {
            giveItem(player, item);
        }
        for (ItemStack item : snapshot.getArmor()) {
            giveItem(player, item);
        }
        giveItem(player, snapshot.getOffhand());
    }

    private void giveItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        player.getInventory().addItem(item.clone()).values()
                .forEach(remaining -> player.getWorld().dropItemNaturally(player.getLocation(), remaining));
    }

    private void setTotalExperience(Player player, int totalExperience) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        if (totalExperience > 0) {
            player.giveExp(totalExperience);
        }
    }

    private void saveSnapshot(RefundSnapshot snapshot) {
        FileConfiguration config = loadConfig(snapshot.getPlayerUuid());
        config.set(path(snapshot.getId(), "playerName"), snapshot.getPlayerName());
        config.set(path(snapshot.getId(), "createdAt"), snapshot.getCreatedAt());
        config.set(path(snapshot.getId(), "expiresAt"), snapshot.getExpiresAt());
        config.set(path(snapshot.getId(), "saveReason"), snapshot.getSaveReason());
        config.set(path(snapshot.getId(), "deathReason"), snapshot.getDeathReason());
        config.set(path(snapshot.getId(), "pendingClaim"), snapshot.isPendingClaim());
        config.set(path(snapshot.getId(), "totalExperience"), snapshot.getTotalExperience());
        config.set(path(snapshot.getId(), "level"), snapshot.getLevel());
        config.set(path(snapshot.getId(), "expProgress"), snapshot.getExpProgress());
        saveLocation(config, snapshot);
        saveItems(config, snapshot.getId(), "inventory", snapshot.getInventory(), 36);
        saveItems(config, snapshot.getId(), "armor", snapshot.getArmor(), 4);
        config.set(path(snapshot.getId(), "offhand"), isRealItem(snapshot.getOffhand()) ? snapshot.getOffhand() : null);
        saveConfig(snapshot.getPlayerUuid(), config);
    }

    private RefundSnapshot loadSnapshot(FileConfiguration config, UUID playerUuid, String id) {
        if (!config.contains("refunds." + id)) {
            return null;
        }

        long expiresAt = config.getLong(path(id, "expiresAt"), 0);
        if (expiresAt > 0 && expiresAt <= System.currentTimeMillis()) {
            return null;
        }

        Location location = loadLocation(config, id);
        return new RefundSnapshot(
                id,
                playerUuid,
                config.getString(path(id, "playerName"), "Unknown"),
                config.getLong(path(id, "createdAt")),
                expiresAt,
                config.getString(path(id, "saveReason"), "UNKNOWN"),
                config.getString(path(id, "deathReason"), ""),
                location,
                loadItems(config, id, "inventory", 36),
                loadItems(config, id, "armor", 4),
                config.getItemStack(path(id, "offhand")),
                config.getInt(path(id, "totalExperience"), 0),
                config.getInt(path(id, "level"), 0),
                (float) config.getDouble(path(id, "expProgress"), 0),
                config.getBoolean(path(id, "pendingClaim"), false)
        );
    }

    private void saveLocation(FileConfiguration config, RefundSnapshot snapshot) {
        Location location = snapshot.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }

        String base = path(snapshot.getId(), "location");
        config.set(base + ".world", location.getWorld().getName());
        config.set(base + ".x", location.getX());
        config.set(base + ".y", location.getY());
        config.set(base + ".z", location.getZ());
        config.set(base + ".yaw", location.getYaw());
        config.set(base + ".pitch", location.getPitch());
    }

    private Location loadLocation(FileConfiguration config, String id) {
        String base = path(id, "location");
        String worldName = config.getString(base + ".world");
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                config.getDouble(base + ".x"),
                config.getDouble(base + ".y"),
                config.getDouble(base + ".z"),
                (float) config.getDouble(base + ".yaw"),
                (float) config.getDouble(base + ".pitch")
        );
    }

    private void saveItems(FileConfiguration config, String id, String type, ItemStack[] items, int size) {
        for (int i = 0; i < size; i++) {
            ItemStack item = i < items.length ? items[i] : null;
            config.set(path(id, type + "." + i), isRealItem(item) ? item : null);
        }
    }

    private ItemStack[] loadItems(FileConfiguration config, String id, String type, int size) {
        ItemStack[] items = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = config.getItemStack(path(id, type + "." + i));
        }
        return items;
    }

    private ItemStack[] cloneItems(ItemStack[] source, int size) {
        ItemStack[] clone = new ItemStack[size];
        if (source == null) {
            return clone;
        }

        for (int i = 0; i < size && i < source.length; i++) {
            clone[i] = cloneItem(source[i]);
        }
        return clone;
    }

    private ItemStack cloneItem(ItemStack item) {
        return isRealItem(item) ? item.clone() : null;
    }

    private boolean isRealItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    private String path(String id, String field) {
        return "refunds." + id + "." + field;
    }

    private FileConfiguration loadConfig(UUID playerUuid) {
        return YamlConfiguration.loadConfiguration(getFile(playerUuid));
    }

    private void saveConfig(UUID playerUuid, FileConfiguration config) {
        try {
            config.save(getFile(playerUuid));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save refund data for " + playerUuid + ": " + e.getMessage());
        }
    }

    private File getFile(UUID playerUuid) {
        return new File(dataFolder, playerUuid + ".yml");
    }
}
