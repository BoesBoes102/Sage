package com.boes.sage.features.warp;

import com.boes.sage.Sage;
import com.boes.sage.features.warp.data.Warp;
import com.boes.sage.Utils.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpService {

    private final Sage plugin;
    private final Map<String, Warp> warps = new HashMap<>();
    private final DatabaseManager databaseManager;

    public WarpService(Sage plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        loadWarps();
    }

    public void createWarp(String name, Location location) {
        Warp warp = new Warp(name, location, false);
        warps.put(name.toLowerCase(), warp);
        saveWarp(warp);
    }

    public boolean deleteWarp(String name) {
        Warp removed = warps.remove(name.toLowerCase());
        if (removed != null) {
            String lowerName = name.toLowerCase();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection connection = databaseManager.getConnection();
                     PreparedStatement statement = connection.prepareStatement("DELETE FROM warps WHERE name = ?")) {
                    statement.setString(1, lowerName);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to delete warp " + lowerName + ": " + e.getMessage());
                }
            });
            return true;
        }
        return false;
    }

    public boolean setWarpLocation(String name, Location location) {
        Warp warp = warps.get(name.toLowerCase());
        if (warp == null) {
            return false;
        }
        warp.setLocation(location);
        saveWarp(warp);
        return true;
    }

    public boolean setHidden(String name, boolean hidden) {
        Warp warp = warps.get(name.toLowerCase());
        if (warp == null) {
            return false;
        }
        warp.setHidden(hidden);
        saveWarp(warp);
        return true;
    }

    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public Collection<Warp> getWarps() {
        return Collections.unmodifiableCollection(warps.values());
    }

    public List<Warp> getVisibleWarps() {
        List<Warp> visible = new ArrayList<>();
        for (Warp warp : warps.values()) {
            if (!warp.isHidden()) {
                visible.add(warp);
            }
        }
        return visible;
    }

    private void saveWarp(Warp warp) {
        String name = warp.getName().toLowerCase();
        String worldName = warp.getWorldName();
        double x = warp.getX();
        double y = warp.getY();
        double z = warp.getZ();
        float yaw = warp.getYaw();
        float pitch = warp.getPitch();
        boolean hidden = warp.isHidden();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO warps (name, world, x, y, z, yaw, pitch, hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT(name) DO UPDATE SET world = excluded.world, x = excluded.x, y = excluded.y, " +
                         "z = excluded.z, yaw = excluded.yaw, pitch = excluded.pitch, hidden = excluded.hidden")) {
                statement.setString(1, name);
                statement.setString(2, worldName);
                statement.setDouble(3, x);
                statement.setDouble(4, y);
                statement.setDouble(5, z);
                statement.setFloat(6, yaw);
                statement.setFloat(7, pitch);
                statement.setInt(8, hidden ? 1 : 0);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save warp " + name + ": " + e.getMessage());
            }
        });
    }

    private void loadWarps() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT name, world, x, y, z, yaw, pitch, hidden FROM warps");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String warpName = resultSet.getString("name");
                try {
                    String worldName = resultSet.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("Warp '" + warpName + "' references missing world '" + worldName + "'. Skipping.");
                        continue;
                    }

                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float yaw = resultSet.getFloat("yaw");
                    float pitch = resultSet.getFloat("pitch");
                    boolean hidden = resultSet.getInt("hidden") != 0;

                    Location location = new Location(world, x, y, z, yaw, pitch);
                    warps.put(warpName.toLowerCase(), new Warp(warpName, location, hidden));
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading warp '" + warpName + "': " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load warps", e);
        }
    }

    public List<String> getWarpNames(boolean includeHidden) {
        List<String> names = new ArrayList<>();
        for (Warp warp : warps.values()) {
            if (!warp.isHidden() || includeHidden) {
                names.add(warp.getName());
            }
        }
        Collections.sort(names);
        return names;
    }
}
