package com.boes.sage.listeners;

import com.boes.sage.Sage;
import com.boes.sage.managers.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class VanishListener implements Listener {
    private final Sage plugin;

    public VanishListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VanishManager vanishManager = plugin.getVanishManager();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            vanishManager.updatePlayerVisibility(player);

            if (vanishManager.isVanished(player)) {
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (!online.hasPermission("sage.vanish.see") && !online.equals(player)) {
                        online.hidePlayer(plugin, player);
                    }
                }
                updatePlayerListName(player, true);
            }

            for (UUID uuid : vanishManager.getVanishedPlayers()) {
                Player vanishedPlayer = plugin.getServer().getPlayer(uuid);
                if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                    updatePlayerListName(vanishedPlayer, true);
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        VanishManager vanishManager = plugin.getVanishManager();

        if (vanishManager.isVanished(player)) {
            vanishManager.setVanished(player, false);
        }
    }

    private void updatePlayerListName(Player player, boolean vanished) {
        if (vanished) {
            player.setPlayerListName("§7[VANISHED] §f" + player.getName());
        } else {
            player.setPlayerListName(player.getName());
        }
    }
}