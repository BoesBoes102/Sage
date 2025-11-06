package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpyManager {
    private final Sage plugin;
    private final Set<UUID> consoleSpy;
    private final Set<UUID> commandSpy;
    private final JsonStorageManager storageManager;

    public SpyManager(Sage plugin) {
        this.plugin = plugin;
        this.consoleSpy = new HashSet<>();
        this.commandSpy = new HashSet<>();
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "spy-data.json"));
        loadData();
    }

    private void loadData() {
        JsonObject json = storageManager.load();

        if (json.has("console-spy")) {
            for (int i = 0; i < json.getAsJsonArray("console-spy").size(); i++) {
                try {
                    consoleSpy.add(UUID.fromString(json.getAsJsonArray("console-spy").get(i).getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        if (json.has("command-spy")) {
            for (int i = 0; i < json.getAsJsonArray("command-spy").size(); i++) {
                try {
                    commandSpy.add(UUID.fromString(json.getAsJsonArray("command-spy").get(i).getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveData() {
        JsonObject json = storageManager.load();
        JsonArray consoleSpyArray = new JsonArray();
        JsonArray commandSpyArray = new JsonArray();

        consoleSpy.forEach(uuid -> consoleSpyArray.add(uuid.toString()));
        commandSpy.forEach(uuid -> commandSpyArray.add(uuid.toString()));

        json.add("console-spy", consoleSpyArray);
        json.add("command-spy", commandSpyArray);

        storageManager.save(json);
    }

    public boolean hasConsoleSpy(Player player) {
        return consoleSpy.contains(player.getUniqueId());
    }

    public boolean hasCommandSpy(Player player) {
        return commandSpy.contains(player.getUniqueId());
    }

    public void setConsoleSpy(Player player, boolean enabled) {
        if (enabled) {
            consoleSpy.add(player.getUniqueId());
        } else {
            consoleSpy.remove(player.getUniqueId());
        }
        saveData();
    }

    public void setCommandSpy(Player player, boolean enabled) {
        if (enabled) {
            commandSpy.add(player.getUniqueId());
        } else {
            commandSpy.remove(player.getUniqueId());
        }
        saveData();
    }

    public boolean toggleConsoleSpy(Player player) {
        boolean newState = !hasConsoleSpy(player);
        setConsoleSpy(player, newState);
        return newState;
    }

    public boolean toggleCommandSpy(Player player) {
        boolean newState = !hasCommandSpy(player);
        setCommandSpy(player, newState);
        return newState;
    }

    public Set<UUID> getConsoleSpyPlayers() {
        return new HashSet<>(consoleSpy);
    }

    public Set<UUID> getCommandSpyPlayers() {
        return new HashSet<>(commandSpy);
    }
}