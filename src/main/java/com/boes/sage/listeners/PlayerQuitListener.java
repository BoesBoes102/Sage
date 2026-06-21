package com.boes.sage.listeners;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final Sage plugin;

    public PlayerQuitListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getOpenInventoryCommand() != null) {
            plugin.getOpenInventoryCommand().handlePlayerQuit(player);
        }
        
        if (plugin.getOpenEnderChestCommand() != null) {
            plugin.getOpenEnderChestCommand().handlePlayerQuit(player);
        }
        
    }
}
