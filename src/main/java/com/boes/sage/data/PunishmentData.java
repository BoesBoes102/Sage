package com.boes.sage.data;

import java.util.HashMap;
import java.util.Map;

public class PunishmentData {
    private final String reason;
    private final Map<Integer, StackPunishment> stackPunishments;

    public PunishmentData(String reason) {
        this.reason = reason;
        this.stackPunishments = new HashMap<>();
    }

    public void addStackPunishment(int stack, String type, String duration) {
        stackPunishments.put(stack, new StackPunishment(type, duration));
    }

    public String getReason() {
        return reason;
    }

    public StackPunishment getPunishment(int stack) {
        return stackPunishments.get(stack);
    }

    public record StackPunishment(String type, String duration) {
    }
}