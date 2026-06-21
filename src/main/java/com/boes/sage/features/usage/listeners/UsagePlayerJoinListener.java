package com.boes.sage.features.usage.listeners;

import com.boes.sage.Sage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UsagePlayerJoinListener implements Listener {
    private final Sage plugin;

    public UsagePlayerJoinListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getUsageBossBarService().restorePlayerBossBar(event.getPlayer());
    }
}
