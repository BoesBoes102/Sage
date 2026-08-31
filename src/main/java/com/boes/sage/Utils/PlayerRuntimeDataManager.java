package com.boes.sage.Utils;

import com.boes.sage.Sage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerRuntimeDataManager {
    private final DatabaseManager databaseManager;

    public PlayerRuntimeDataManager(Sage plugin) {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void setPlayerTime(UUID uuid, long time) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO player_times (uuid, time) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET time = excluded.time")) {
            statement.setString(1, uuid.toString());
            statement.setLong(2, time);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player time for " + uuid, e);
        }
    }

    public void clearPlayerTime(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM player_times WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear player time for " + uuid, e);
        }
    }

    public Long getPlayerTime(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT time FROM player_times WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getLong("time");
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public void setPlayerWeather(UUID uuid, String weather) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO player_weather (uuid, weather) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET weather = excluded.weather")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, weather);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player weather for " + uuid, e);
        }
    }

    public void clearPlayerWeather(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM player_weather WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear player weather for " + uuid, e);
        }
    }

    public String getPlayerWeather(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT weather FROM player_weather WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("weather") : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public void setPendingTeleport(UUID uuid, String location) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO pending_teleports (uuid, location) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET location = excluded.location")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, location);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save pending teleport for " + uuid, e);
        }
    }

    public void clearPendingTeleport(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM pending_teleports WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear pending teleport for " + uuid, e);
        }
    }

    public String getPendingTeleport(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT location FROM pending_teleports WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("location") : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }
}
