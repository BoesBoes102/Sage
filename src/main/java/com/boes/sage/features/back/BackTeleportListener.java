package com.boes.sage.features.back;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BackTeleportListener implements Listener {

    private final BackService backService;

    public BackTeleportListener(BackService backService) {
        this.backService = backService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        backService.recordLocation(player, event.getFrom());
    }
}
