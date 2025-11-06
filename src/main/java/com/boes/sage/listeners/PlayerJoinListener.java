package com.boes.sage.listeners;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final Sage plugin;

    public PlayerJoinListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!player.hasPermission("sage.fly.keep")) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        long playerTime = plugin.getConfig().getLong("player-times." + player.getUniqueId().toString(), -1);
        if (playerTime >= 0) {
            player.setPlayerTime(playerTime, false);
        }

        String playerWeather = plugin.getConfig().getString("player-weather." + player.getUniqueId().toString());
        if (playerWeather != null) {
            switch (playerWeather.toLowerCase()) {
                case "clear":
                    player.setPlayerWeather(org.bukkit.WeatherType.CLEAR);
                    break;
                case "rain":
                    player.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL);
                    break;
                case "thunder":
                    player.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL);
                    break;
            }
        }

        String pendingTeleport = plugin.getConfig().getString("pending-teleports." + player.getUniqueId().toString());
        if (pendingTeleport != null) {
            String[] parts = pendingTeleport.split(";");
            if (parts.length == 6) {
                try {
                    World world = Bukkit.getWorld(parts[0]);
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    float yaw = Float.parseFloat(parts[4]);
                    float pitch = Float.parseFloat(parts[5]);

                    if (world != null) {
                        Location location = new Location(world, x, y, z, yaw, pitch);
                        player.teleport(location);
                        player.sendMessage("§aYou have been teleported to a pending location!");
                    }
                } catch (NumberFormatException e) {
                }
            }

            plugin.getConfig().set("pending-teleports." + player.getUniqueId().toString(), null);
            plugin.saveConfig();
        }
    }
}