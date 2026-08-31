package com.boes.sage.features.alts.listeners;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AltsListener implements Listener {
    private final Sage plugin;

    public AltsListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getAltAccountService() != null) {
            plugin.getAltAccountService().trackLogin(player);
        }
    }
}
