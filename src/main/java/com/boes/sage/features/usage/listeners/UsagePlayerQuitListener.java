package com.boes.sage.features.usage.listeners;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class UsagePlayerQuitListener implements Listener {
    private final Sage plugin;

    public UsagePlayerQuitListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getUsageBossBarService().hasBossBar(player.getUniqueId())) {
            plugin.getUsageBossBarService().removeBossBar(player);
        }
    }
}
