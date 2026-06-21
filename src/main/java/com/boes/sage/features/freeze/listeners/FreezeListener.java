package com.boes.sage.features.freeze.listeners;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {
    private final Sage plugin;

    public FreezeListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getFreezeService().isFrozen(event.getPlayer())) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        event.setTo(event.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getFreezeService().isFrozen(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player damager = extractDamager(event);
        if (damager == null) {
            return;
        }

        if (plugin.getFreezeService().isFrozen(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getFreezeService().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        event.getRecipients().removeIf(recipient -> plugin.getFreezeService().isFrozen(recipient.getUniqueId()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getFreezeService().isFrozen(event.getPlayer())) {
            plugin.getFreezeService().freeze(event.getPlayer());
        }
    }

    private Player extractDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }

        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }

        return null;
    }
}
