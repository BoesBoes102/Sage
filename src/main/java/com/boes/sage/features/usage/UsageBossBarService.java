package com.boes.sage.features.usage;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UsageBossBarService {
    private final Sage plugin;
    private final Set<UUID> enabledPlayers;
    private final Map<UUID, BossBar> playerBossBars;
    private final JsonStorageManager storageManager;
    private BukkitTask updateTask;

    public UsageBossBarService(Sage plugin) {
        this.plugin = plugin;
        this.enabledPlayers = new HashSet<>();
        this.playerBossBars = new HashMap<>();
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "usage-bossbar.json"));
        loadData();
        startUpdateTask();
    }

    private void loadData() {
        JsonObject json = storageManager.load();
        
        if (json.has("enabled-players")) {
            for (int i = 0; i < json.getAsJsonArray("enabled-players").size(); i++) {
                try {
                    UUID uuid = UUID.fromString(json.getAsJsonArray("enabled-players").get(i).getAsString());
                    enabledPlayers.add(uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        createBossBar(player);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveData() {
        JsonObject json = storageManager.load();
        JsonArray playersArray = new JsonArray();
        
        enabledPlayers.forEach(uuid -> playersArray.add(uuid.toString()));
        
        json.add("enabled-players", playersArray);
        storageManager.save(json);
    }

    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllBossBars, 20L, 20L);
    }

    private void updateAllBossBars() {
        try {
            double currentTps = getTPS();
            double mspt = getMSPT();
            
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            long usedMemory = heapMemory.getUsed();
            long maxMemory = heapMemory.getMax();
            double ramPercentage = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
            
            for (UUID uuid : new HashSet<>(playerBossBars.keySet())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    updateBossBar(uuid, currentTps, mspt, usedMemory, maxMemory, ramPercentage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBossBar(UUID uuid, double tps, double mspt, long usedMemory, long maxMemory, double ramPercentage) {
        BossBar bossBar = playerBossBars.get(uuid);
        if (bossBar == null) return;

        StringBuilder title = new StringBuilder();
        title.append("\u00A77TPS: ");
        title.append(getTpsDisplayColor(tps)).append(String.format(Locale.US, "%.2f", tps));
        title.append(" \u00A77MSPT: ").append(getMsptDisplayColor(mspt)).append(String.format(Locale.US, "%.2f", mspt));
        title.append("ms \u00A77RAM: ").append(getRamDisplayColor(ramPercentage))
                .append(String.format(Locale.US, "%.1f/%.1fGB", bytesToGb(usedMemory), bytesToGb(maxMemory)));

        bossBar.setTitle(title.toString());
        
        double ramProgress = Math.min(ramPercentage / 100.0, 1.0);
        bossBar.setProgress(ramProgress);
        
        if (ramPercentage > 80) {
            bossBar.setColor(BarColor.RED);
        } else if (ramPercentage > 60) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.GREEN);
        }
    }

    public void createBossBar(Player player) {
        if (playerBossBars.containsKey(player.getUniqueId())) {
            return;
        }
        
        BossBar bossBar = Bukkit.createBossBar("Usage Stats", BarColor.GREEN, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        playerBossBars.put(player.getUniqueId(), bossBar);
        
        enabledPlayers.add(player.getUniqueId());
        saveData();
    }

    public void removeBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bossBar = playerBossBars.remove(uuid);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
        
        enabledPlayers.remove(uuid);
        saveData();
    }

    public void toggleBossBar(Player player) {
        if (playerBossBars.containsKey(player.getUniqueId())) {
            removeBossBar(player);
        } else {
            createBossBar(player);
        }
    }

    public boolean hasBossBar(UUID uuid) {
        return playerBossBars.containsKey(uuid);
    }

    public void restorePlayerBossBar(Player player) {
        if (enabledPlayers.contains(player.getUniqueId())) {
            createBossBar(player);
        }
    }

    public void cleanup() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.removeAll();
        }
        playerBossBars.clear();
    }

    private String getTpsDisplayColor(double tps) {
        if (tps >= 19.5) return "\u00A7a";
        if (tps >= 18.0) return "\u00A7e";
        return "\u00A7c";
    }

    private String getMsptDisplayColor(double mspt) {
        if (mspt <= 50.0) return "\u00A7a";
        if (mspt <= 75.0) return "\u00A7e";
        return "\u00A7c";
    }

    private String getRamDisplayColor(double percentage) {
        if (percentage <= 60) return "\u00A7a";
        if (percentage <= 80) return "\u00A7e";
        return "\u00A7c";
    }

    private double getTPS() {
        try {
            double[] tps = plugin.getServer().getTPS();
            if (tps.length == 0) return 20.0;
            return Math.max(0.0, Math.min(20.0, tps[0]));
        } catch (Exception e) {
            return 20.0;
        }
    }

    private double getMSPT() {
        try {
            return Math.max(0.0, plugin.getServer().getAverageTickTime());
        } catch (Exception e) {
            return 50.0;
        }
    }

    private double bytesToGb(long bytes) {
        if (bytes <= 0) return 0.0;
        return bytes / 1024.0 / 1024.0 / 1024.0;
    }
}
