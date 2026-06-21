package com.boes.sage.features.notification.listeners;

import com.boes.sage.Sage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NotificationJoinListener implements Listener {
    private final Sage plugin;

    public NotificationJoinListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getNotificationService() == null) {
            return;
        }

        plugin.getNotificationService().handlePlayerJoin(event.getPlayer());
        plugin.getNotificationService().deliverNotifications(event.getPlayer());
    }
}
