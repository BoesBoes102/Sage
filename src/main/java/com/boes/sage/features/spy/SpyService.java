package com.boes.sage.features.spy;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpyService {
    private final Sage plugin;
    private final DatabaseManager databaseManager;
    private final Set<UUID> commandSpy;

    public SpyService(Sage plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.commandSpy = new HashSet<>();
        loadData();
    }

    private void loadData() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM spy_command");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    commandSpy.add(UUID.fromString(resultSet.getString("uuid")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load spy data", e);
        }
    }

    public boolean hasCommandSpy(Player player) {
        return commandSpy.contains(player.getUniqueId());
    }

    public void setCommandSpy(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (enabled) {
            commandSpy.add(uuid);
        } else {
            commandSpy.remove(uuid);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection()) {
                if (enabled) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT OR IGNORE INTO spy_command (uuid) VALUES (?)")) {
                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM spy_command WHERE uuid = ?")) {
                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save command spy state for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public boolean toggleCommandSpy(Player player) {
        boolean newState = !hasCommandSpy(player);
        setCommandSpy(player, newState);
        return newState;
    }

    public Set<UUID> getCommandSpyPlayers() {
        return new HashSet<>(commandSpy);
    }
}
