package com.boes.sage.managers;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final Sage plugin;
    private final Set<UUID> vanishedPlayers;

    public VanishManager(Sage plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            hidePlayer(player);
            updatePlayerListName(player, true);
            applyGlowEffect(player, true);
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            showPlayer(player);
            updatePlayerListName(player, false);
            applyGlowEffect(player, false);
        }
    }

    private void hidePlayer(Player vanishedPlayer) {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.hasPermission("sage.vanish.see")) {
                online.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    private void showPlayer(Player player) {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }

    public void updatePlayerVisibility(Player player) {
        for (UUID uuid : vanishedPlayers) {
            Player vanishedPlayer = plugin.getServer().getPlayer(uuid);
            if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                if (player.hasPermission("sage.vanish.see")) {
                    player.showPlayer(plugin, vanishedPlayer);
                } else {
                    player.hidePlayer(plugin, vanishedPlayer);
                }
            }
        }
    }

    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }

    private void updatePlayerListName(Player player, boolean vanished) {
        if (vanished) {
            player.setPlayerListName("§7[VANISHED] §f" + player.getName());
        } else {
            player.setPlayerListName(player.getName());
        }
    }

    private void applyGlowEffect(Player player, boolean glow) {
        if (glow) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }
}