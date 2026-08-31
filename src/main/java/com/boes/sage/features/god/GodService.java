package com.boes.sage.features.god;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodService {
    private final Set<UUID> godPlayers = new HashSet<>();

    public boolean hasGod(Player player) {
        return godPlayers.contains(player.getUniqueId());
    }

    public boolean hasGod(UUID uuid) {
        return godPlayers.contains(uuid);
    }

    public void setGod(Player player, boolean enabled) {
        if (enabled) {
            godPlayers.add(player.getUniqueId());
        } else {
            godPlayers.remove(player.getUniqueId());
        }
    }

    public boolean toggleGod(Player player) {
        boolean newState = !hasGod(player);
        setGod(player, newState);
        return newState;
    }

    public void clear(UUID uuid) {
        godPlayers.remove(uuid);
    }
}
