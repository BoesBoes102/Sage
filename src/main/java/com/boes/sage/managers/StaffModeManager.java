package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.boes.sage.data.StaffModeData;
import com.google.gson.JsonObject;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class StaffModeManager {
    private final Sage plugin;
    private final Map<UUID, StaffModeData> staffModePlayers;
    private final JsonStorageManager storageManager;

    public StaffModeManager(Sage plugin) {
        this.plugin = plugin;
        this.staffModePlayers = new HashMap<>();
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "staffmode.json"));

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

        if (!plugin.getVanishManager().isVanished(player)) {
            plugin.getVanishManager().setVanished(player, true);
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

        if (plugin.getVanishManager().isVanished(player)) {
            plugin.getVanishManager().setVanished(player, false);
        }

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }

    private void saveStaffModeData(UUID uuid, StaffModeData data) {
        JsonObject json = storageManager.load();
        JsonObject playerData = new JsonObject();

        JsonObject location = new JsonObject();
        location.addProperty("world", data.getLocation().getWorld().getName());
        location.addProperty("x", data.getLocation().getX());
        location.addProperty("y", data.getLocation().getY());
        location.addProperty("z", data.getLocation().getZ());
        location.addProperty("yaw", data.getLocation().getYaw());
        location.addProperty("pitch", data.getLocation().getPitch());

        playerData.add("location", location);
        playerData.addProperty("gamemode", data.getGameMode().name());

        json.add(uuid.toString(), playerData);
        storageManager.save(json);
    }

    private void removeStaffModeData(UUID uuid) {
        JsonObject json = storageManager.load();
        json.remove(uuid.toString());
        storageManager.save(json);
    }

    private void loadStaffModePlayers() {
        JsonObject json = storageManager.load();

        for (String key : json.keySet()) {
            try {
                UUID uuid = UUID.fromString(key);
                Player player = plugin.getServer().getPlayer(uuid);

                if (player != null && player.isOnline()) {
                    JsonObject playerData = json.getAsJsonObject(key);
                    JsonObject locationData = playerData.getAsJsonObject("location");

                    String worldName = locationData.get("world").getAsString();
                    double x = locationData.get("x").getAsDouble();
                    double y = locationData.get("y").getAsDouble();
                    double z = locationData.get("z").getAsDouble();
                    float yaw = locationData.get("yaw").getAsFloat();
                    float pitch = locationData.get("pitch").getAsFloat();

                    Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);

                    GameMode gameMode = GameMode.valueOf(playerData.get("gamemode").getAsString());

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