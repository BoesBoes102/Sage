package com.boes.sage.listeners;

import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinSyncListener implements Listener {
    private final Sage plugin;

    public PlayerJoinSyncListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        OfflinePlayerDataManager.syncOnPlayerJoin(event.getPlayer());
        
        plugin.getNotificationManager().deliverNotifications(event.getPlayer());
    }
}