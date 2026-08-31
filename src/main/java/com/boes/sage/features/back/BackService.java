package com.boes.sage.features.back;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackService {

    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private final Set<UUID> suppressed = ConcurrentHashMap.newKeySet();

    public void suppressNextRecord(UUID playerId) {
        suppressed.add(playerId);
    }

    public void recordLocation(Player player, Location location) {
        if (suppressed.remove(player.getUniqueId())) {
            return;
        }
        lastLocations.put(player.getUniqueId(), location);
    }

    public Location getLastLocation(Player player) {
        return lastLocations.get(player.getUniqueId());
    }

    public void setLastLocation(Player player, Location location) {
        lastLocations.put(player.getUniqueId(), location);
    }
}
