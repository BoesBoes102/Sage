package com.boes.sage.features.openinv.listeners;

import com.boes.sage.Sage;
import com.boes.sage.features.openinv.OpenEnderChestService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OpenEnderChestListener implements Listener {
    private final OpenEnderChestService service;

    public OpenEnderChestListener(Sage plugin) {
        this.service = plugin.getOpenEnderChestService();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        service.handleInventoryClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        service.handleInventoryDrag(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        service.handleInventoryClose(event);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        service.prepareForPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        service.handlePlayerQuit(event.getPlayer());
    }
}
