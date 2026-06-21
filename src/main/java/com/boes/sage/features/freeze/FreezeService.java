package com.boes.sage.features.freeze;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeService {
    private static final String DEFAULT_MESSAGE = "&cYou are currently frozen.";
    private static final String DEFAULT_TITLE = "&c&lFROZEN";
    private static final String DEFAULT_SUBTITLE = "&7You cannot move or use commands.";

    private final Sage plugin;
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private final BukkitTask titleTask;

    public FreezeService(Sage plugin) {
        this.plugin = plugin;
        long refreshTicks = Math.max(20L, plugin.getConfig().getLong("freeze.title-refresh-ticks", 40L));
        this.titleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshTitles, 0L, refreshTicks);
    }

    public boolean toggleFreeze(Player target) {
        if (isFrozen(target)) {
            unfreeze(target);
            return false;
        }

        freeze(target);
        return true;
    }

    public void freeze(Player target) {
        frozenPlayers.add(target.getUniqueId());
        sendFreezeState(target);
    }

    public void unfreeze(Player target) {
        frozenPlayers.remove(target.getUniqueId());
        target.resetTitle();
    }

    public boolean isFrozen(Player player) {
        return isFrozen(player.getUniqueId());
    }

    public boolean isFrozen(UUID playerId) {
        return frozenPlayers.contains(playerId);
    }

    public void shutdown() {
        titleTask.cancel();
        for (UUID playerId : frozenPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.resetTitle();
            }
        }
        frozenPlayers.clear();
    }

    public String getFreezeMessage() {
        return colorize(plugin.getConfig().getString("freeze.message", DEFAULT_MESSAGE));
    }

    private void refreshTitles() {
        for (UUID playerId : frozenPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                continue;
            }

            sendFreezeTitle(player);
        }
    }

    private void sendFreezeState(Player player) {
        player.sendMessage(getFreezeMessage());
        sendFreezeTitle(player);
    }

    private void sendFreezeTitle(Player player) {
        FileConfiguration config = plugin.getConfig();
        String title = colorize(config.getString("freeze.title", DEFAULT_TITLE));
        String subtitle = colorize(config.getString("freeze.subtitle", DEFAULT_SUBTITLE));
        int stayTicks = (int) Math.max(40L, config.getLong("freeze.title-stay-ticks", 80L));
        player.sendTitle(title, subtitle, 0, stayTicks, 0);
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
