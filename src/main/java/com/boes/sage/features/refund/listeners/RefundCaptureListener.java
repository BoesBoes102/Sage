package com.boes.sage.features.refund.listeners;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RefundCaptureListener implements Listener {
    private final Sage plugin;

    public RefundCaptureListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRefundService().saveSnapshot(player, "JOIN", "", player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRefundService().saveSnapshot(player, "LEAVE", "", player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathReason = event.getDeathMessage();
        if (deathReason == null || deathReason.isBlank()) {
            deathReason = player.getLastDamageCause() == null
                    ? "Unknown death"
                    : player.getLastDamageCause().getCause().name();
        }

        plugin.getRefundService().saveSnapshot(player, "DEATH", deathReason, player.getLocation());
    }
}
