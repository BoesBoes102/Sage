package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.data.Warp;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpManager {

    private final Sage plugin;
    private final Map<String, Warp> warps = new HashMap<>();
    private final JsonStorageManager storageManager;

    public WarpManager(Sage plugin) {
        this.plugin = plugin;
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "warps.json"));
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
            JsonObject json = storageManager.load();
            json.remove(name.toLowerCase());
            storageManager.save(json);
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
        JsonObject json = storageManager.load();
        JsonObject warpObj = new JsonObject();
        warpObj.addProperty("world", warp.getWorldName());
        warpObj.addProperty("x", warp.getX());
        warpObj.addProperty("y", warp.getY());
        warpObj.addProperty("z", warp.getZ());
        warpObj.addProperty("yaw", warp.getYaw());
        warpObj.addProperty("pitch", warp.getPitch());
        warpObj.addProperty("hidden", warp.isHidden());
        json.add(warp.getName().toLowerCase(), warpObj);
        storageManager.save(json);
    }

    private void loadWarps() {
        JsonObject json = storageManager.load();
        
        for (String warpName : json.keySet()) {
            try {
                JsonObject warpObj = json.getAsJsonObject(warpName);
                String worldName = warpObj.get("world").getAsString();
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Warp '" + warpName + "' references missing world '" + worldName + "'. Skipping.");
                    continue;
                }

                double x = warpObj.get("x").getAsDouble();
                double y = warpObj.get("y").getAsDouble();
                double z = warpObj.get("z").getAsDouble();
                float yaw = warpObj.get("yaw").getAsFloat();
                float pitch = warpObj.get("pitch").getAsFloat();
                boolean hidden = warpObj.get("hidden").getAsBoolean();

                Location location = new Location(world, x, y, z, yaw, pitch);
                warps.put(warpName.toLowerCase(), new Warp(warpName, location, hidden));
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading warp '" + warpName + "': " + e.getMessage());
            }
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