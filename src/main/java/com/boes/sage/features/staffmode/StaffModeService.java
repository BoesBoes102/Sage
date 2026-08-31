package com.boes.sage.features.staffmode;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
import com.boes.sage.features.staffmode.data.StaffModeData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StaffModeService {
    private final Sage plugin;
    private final Map<UUID, StaffModeData> staffModePlayers;
    private final DatabaseManager databaseManager;

    public StaffModeService(Sage plugin) {
        this.plugin = plugin;
        this.staffModePlayers = new HashMap<>();
        this.databaseManager = plugin.getDatabaseManager();

        loadStaffModePlayers();
    }

    public boolean isInStaffMode(Player player) {
        return staffModePlayers.containsKey(player.getUniqueId());
    }

    public void enableStaffMode(Player player) {
        if (isInStaffMode(player)) {
            return;
        }

        Location location = player.getLocation();
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        GameMode gameMode = player.getGameMode();

        StaffModeData data = new StaffModeData(
                location,
                inventory,
                armor,
                gameMode
        );

        staffModePlayers.put(player.getUniqueId(), data);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);

        saveStaffModeData(player.getUniqueId(), data);

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.hasPermission("sage.staff")) {
                online.hidePlayer(plugin, player);
            }
        }

        if (!plugin.getVanishService().isVanished(player)) {
            plugin.getVanishService().setVanished(player, true);
        }
    }

    public void disableStaffMode(Player player) {
        if (!isInStaffMode(player)) {
            return;
        }

        StaffModeData data = staffModePlayers.remove(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        player.getInventory().setContents(data.getInventory());
        player.getInventory().setArmorContents(data.getArmor());
        player.setGameMode(data.getGameMode());
        player.teleport(data.getLocation());

        if (data.getGameMode() != GameMode.CREATIVE && data.getGameMode() != GameMode.SPECTATOR) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        removeStaffModeData(player.getUniqueId());

        if (plugin.getVanishService().isVanished(player)) {
            plugin.getVanishService().setVanished(player, false);
        }

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }

    private void saveStaffModeData(UUID uuid, StaffModeData data) {
        Location location = data.getLocation();
        String worldName = Objects.requireNonNull(location.getWorld()).getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        String gameMode = data.getGameMode().name();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO staffmode (uuid, world, x, y, z, yaw, pitch, gamemode) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT(uuid) DO UPDATE SET world = excluded.world, x = excluded.x, y = excluded.y, " +
                         "z = excluded.z, yaw = excluded.yaw, pitch = excluded.pitch, gamemode = excluded.gamemode")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, worldName);
                statement.setDouble(3, x);
                statement.setDouble(4, y);
                statement.setDouble(5, z);
                statement.setFloat(6, yaw);
                statement.setFloat(7, pitch);
                statement.setString(8, gameMode);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save staff mode data for " + uuid + ": " + e.getMessage());
            }
        });
    }

    private void removeStaffModeData(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM staffmode WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove staff mode data for " + uuid + ": " + e.getMessage());
            }
        });
    }

    private void loadStaffModePlayers() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid, world, x, y, z, yaw, pitch, gamemode FROM staffmode");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String key = resultSet.getString("uuid");
                try {
                    UUID uuid = UUID.fromString(key);
                    Player player = plugin.getServer().getPlayer(uuid);

                    if (player != null && player.isOnline()) {
                        String worldName = resultSet.getString("world");
                        double x = resultSet.getDouble("x");
                        double y = resultSet.getDouble("y");
                        double z = resultSet.getDouble("z");
                        float yaw = resultSet.getFloat("yaw");
                        float pitch = resultSet.getFloat("pitch");

                        Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);

                        GameMode gameMode = GameMode.valueOf(resultSet.getString("gamemode"));

                        StaffModeData data = new StaffModeData(location, new ItemStack[0], new ItemStack[0], gameMode);
                        staffModePlayers.put(uuid, data);

                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load staff mode data for " + key);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load staff mode players", e);
        }
    }

    public void disableAllStaffMode() {
        for (UUID uuid : new HashSet<>(staffModePlayers.keySet())) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                disableStaffMode(player);
            }
        }
    }
}
