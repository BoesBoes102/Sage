package com.boes.sage.features.freeze;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeService {
    private static final String MESSAGE = "&cYou are currently frozen.";
    private static final String TITLE = "&c&lFROZEN";
    private static final String SUBTITLE = "&7You cannot move or use commands.";
    private static final long TITLE_REFRESH_TICKS = 40L;
    private static final int TITLE_STAY_TICKS = 80;

    private final Sage plugin;
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private final BukkitTask titleTask;

    public FreezeService(Sage plugin) {
        this.plugin = plugin;
        this.titleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshTitles, 0L, TITLE_REFRESH_TICKS);
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
        return colorize(MESSAGE);
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
        player.sendTitle(colorize(TITLE), colorize(SUBTITLE), 0, TITLE_STAY_TICKS, 0);
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
