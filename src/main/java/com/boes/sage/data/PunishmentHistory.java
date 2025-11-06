package com.boes.sage.data;

import java.util.UUID;

public class PunishmentHistory {
    private final String type;
    private final String reason;
    private final String punisher;
    private final long timestamp;
    private final String duration;
    private final UUID targetUUID;

    public PunishmentHistory(String type, String reason, String punisher, long timestamp, String duration, UUID targetUUID) {
        this.type = type;
        this.reason = reason;
        this.punisher = punisher;
        this.timestamp = timestamp;
        this.duration = duration;
        this.targetUUID = targetUUID;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public String getPunisher() {
        return punisher;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDuration() {
        return duration;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }
}