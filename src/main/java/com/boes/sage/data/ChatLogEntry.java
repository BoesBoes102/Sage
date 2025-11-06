package com.boes.sage.data;

import java.util.UUID;

public class ChatLogEntry {
    private final UUID playerUUID;
    private final String playerName;
    private final String message;
    private final String type;
    private final long timestamp;

    public ChatLogEntry(UUID playerUUID, String playerName, String message, String type, long timestamp) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
}