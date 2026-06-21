package com.boes.sage.features.itemedit;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemEditService {
    private final Map<UUID, List<String>> loreClipboard = new HashMap<>();

    public List<String> getLoreClipboard(UUID uniqueId) {
        List<String> lines = loreClipboard.get(uniqueId);
        return lines == null ? null : new ArrayList<>(lines);
    }

    public void setLoreClipboard(UUID uniqueId, List<String> lore) {
        if (lore == null) {
            loreClipboard.remove(uniqueId);
            return;
        }
        loreClipboard.put(uniqueId, new ArrayList<>(lore));
    }

    public String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
