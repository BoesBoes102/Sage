package com.boes.sage.features.chatlog;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
import com.boes.sage.features.chatlog.data.ChatLogEntry;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatLogService {
    private final Map<UUID, List<ChatLogEntry>> chatLogs;
    private final DatabaseManager databaseManager;

    private static final long MESSAGE_RETENTION = 3 * 7 * 24 * 60 * 60 * 1000L;
    private static final long COMMAND_RETENTION = 2 * 7 * 24 * 60 * 60 * 1000L;
    private static final int MESSAGES_PER_PAGE = 30;

    public ChatLogService(Sage plugin) {
        this.chatLogs = new ConcurrentHashMap<>();
        this.databaseManager = plugin.getDatabaseManager();
        loadLogs();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::cleanupExpiredLogs, 3600 * 20, 3600 * 20);
    }

    public void logMessage(UUID playerUUID, String playerName, String message) {
        logEntry(playerUUID, playerName, message, "message");
    }

    public void logCommand(UUID playerUUID, String playerName, String command) {
        logEntry(playerUUID, playerName, command, "command");
    }

    private void logEntry(UUID playerUUID, String playerName, String content, String type) {
        ChatLogEntry entry = new ChatLogEntry(playerUUID, playerName, content, type, System.currentTimeMillis());
        chatLogs.computeIfAbsent(playerUUID, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);
        insertLog(entry);
    }

    private void insertLog(ChatLogEntry entry) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO chat_log (uuid, player_name, message, type, timestamp) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, entry.getPlayerUUID().toString());
            statement.setString(2, entry.getPlayerName());
            statement.setString(3, entry.getMessage());
            statement.setString(4, entry.getType());
            statement.setLong(5, entry.getTimestamp());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert chat log entry", e);
        }
    }

    public List<ChatLogEntry> getLogs(UUID playerUUID, String type) {
        List<ChatLogEntry> allLogs = chatLogs.getOrDefault(playerUUID, new ArrayList<>());

        if ("all".equalsIgnoreCase(type)) {
            return new ArrayList<>(allLogs);
        }

        List<ChatLogEntry> filtered = new ArrayList<>();
        for (ChatLogEntry entry : allLogs) {
            if (entry.getType().equalsIgnoreCase(type)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<ChatLogEntry> getLogsPage(UUID playerUUID, String type, int page) {
        List<ChatLogEntry> logs = getLogs(playerUUID, type);
        int endIndex = logs.size() - ((page - 1) * MESSAGES_PER_PAGE);
        int startIndex = Math.max(endIndex - MESSAGES_PER_PAGE, 0);

        if (endIndex <= 0 || startIndex >= logs.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(logs.subList(startIndex, endIndex));
    }

    public int getTotalPages(UUID playerUUID, String type) {
        List<ChatLogEntry> logs = getLogs(playerUUID, type);
        return (logs.size() + MESSAGES_PER_PAGE - 1) / MESSAGES_PER_PAGE;
    }

    private void cleanupExpiredLogs() {
        long currentTime = System.currentTimeMillis();

        for (UUID playerUUID : chatLogs.keySet()) {
            List<ChatLogEntry> logs = chatLogs.get(playerUUID);
            logs.removeIf(entry -> {
                if ("message".equalsIgnoreCase(entry.getType())) {
                    return (currentTime - entry.getTimestamp()) > MESSAGE_RETENTION;
                } else if ("command".equalsIgnoreCase(entry.getType())) {
                    return (currentTime - entry.getTimestamp()) > COMMAND_RETENTION;
                }
                return false;
            });

            if (logs.isEmpty()) {
                chatLogs.remove(playerUUID);
            }
        }

        long messageCutoff = currentTime - MESSAGE_RETENTION;
        long commandCutoff = currentTime - COMMAND_RETENTION;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM chat_log WHERE (type = 'message' AND timestamp <= ?) OR (type = 'command' AND timestamp <= ?)")) {
            statement.setLong(1, messageCutoff);
            statement.setLong(2, commandCutoff);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean up expired chat logs", e);
        }
    }

    private void loadLogs() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid, player_name, message, type, timestamp FROM chat_log ORDER BY id ASC");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                try {
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    String playerName = resultSet.getString("player_name");
                    String message = resultSet.getString("message");
                    String type = resultSet.getString("type");
                    long timestamp = resultSet.getLong("timestamp");

                    chatLogs.computeIfAbsent(uuid, k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(new ChatLogEntry(uuid, playerName, message, type, timestamp));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load chat logs", e);
        }
    }

    public void saveLogs() {
    }

    public static int getMessagesPerPage() {
        return MESSAGES_PER_PAGE;
    }
}
