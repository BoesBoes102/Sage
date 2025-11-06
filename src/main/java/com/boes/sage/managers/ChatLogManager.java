package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.boes.sage.data.ChatLogEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatLogManager {
    private final Map<UUID, List<ChatLogEntry>> chatLogs;
    private final JsonStorageManager storageManager;
    
    private static final long MESSAGE_RETENTION = 3 * 7 * 24 * 60 * 60 * 1000L;
    private static final long COMMAND_RETENTION = 2 * 7 * 24 * 60 * 60 * 1000L;
    private static final int MESSAGES_PER_PAGE = 30;

    public ChatLogManager(Sage plugin) {
        this.chatLogs = new ConcurrentHashMap<>();
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "chat-logs.json"));
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
        chatLogs.computeIfAbsent(playerUUID, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new ChatLogEntry(playerUUID, playerName, content, type, System.currentTimeMillis()));
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
        int startIndex = (page - 1) * MESSAGES_PER_PAGE;
        int endIndex = Math.min(startIndex + MESSAGES_PER_PAGE, logs.size());
        
        if (startIndex >= logs.size()) {
            return new ArrayList<>();
        }
        
        return logs.subList(startIndex, endIndex);
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
        
        saveLogs();
    }

    private void loadLogs() {
        JsonObject json = storageManager.load();
        
        for (String uuidStr : json.keySet()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                JsonArray logsArray = json.getAsJsonArray(uuidStr);
                List<ChatLogEntry> logs = Collections.synchronizedList(new ArrayList<>());
                
                for (int i = 0; i < logsArray.size(); i++) {
                    try {
                        JsonObject logObj = logsArray.get(i).getAsJsonObject();
                        String playerName = logObj.get("playerName").getAsString();
                        String message = logObj.get("message").getAsString();
                        String type = logObj.get("type").getAsString();
                        long timestamp = logObj.get("timestamp").getAsLong();
                        
                        logs.add(new ChatLogEntry(uuid, playerName, message, type, timestamp));
                    } catch (Exception ignored) {
                    }
                }
                
                if (!logs.isEmpty()) {
                    chatLogs.put(uuid, logs);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveLogs() {
        JsonObject json = new JsonObject();
        
        for (Map.Entry<UUID, List<ChatLogEntry>> entry : chatLogs.entrySet()) {
            JsonArray logsArray = new JsonArray();
            
            for (ChatLogEntry log : entry.getValue()) {
                JsonObject logObj = new JsonObject();
                logObj.addProperty("playerName", log.getPlayerName());
                logObj.addProperty("message", log.getMessage());
                logObj.addProperty("type", log.getType());
                logObj.addProperty("timestamp", log.getTimestamp());
                
                logsArray.add(logObj);
            }
            
            json.add(entry.getKey().toString(), logsArray);
        }
        
        storageManager.save(json);
    }

    public static int getMessagesPerPage() {
        return MESSAGES_PER_PAGE;
    }
}