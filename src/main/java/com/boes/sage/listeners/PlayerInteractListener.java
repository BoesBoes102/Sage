package com.boes.sage.listeners;

import com.boes.sage.Sage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    private final Sage plugin;
    private final Map<UUID, Integer> activeParticleEffects = new HashMap<>();

    public PlayerInteractListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (clickedBlock == null) {
            return;
        }

        Material material = clickedBlock.getType();

        if (!isValidSitBlock(material)) {
            return;
        }

        if (isSittingStick(itemInHand)) {
            if (event.getAction().toString().contains("RIGHT")) {
                event.setCancelled(true);

                if (player.isSneaking()) {
                    activateChairParticles(player);
                    return;
                }

                Location blockCenter = clickedBlock.getLocation().add(0.5, 0.5, 0.5);
                player.getWorld().spawnParticle(Particle.HEART, blockCenter, 10, 0.3, 0.3, 0.3);

                String chairKey = blockLocationToString(clickedBlock.getLocation());
                if (plugin.getMarkedChairs().contains(chairKey)) {
                    plugin.getMarkedChairs().remove(chairKey);
                    player.sendMessage("§cChair unmarked!");
                } else {
                    plugin.getMarkedChairs().add(chairKey);
                    player.sendMessage("§aChair marked!");
                }
            }
            return;
        }

        if (!player.hasPermission("sage.sit.block")) {
            return;
        }

        if (event.getAction().toString().contains("RIGHT")) {
            String chairKey = blockLocationToString(clickedBlock.getLocation());
            if (!plugin.getMarkedChairs().contains(chairKey)) {
                return;
            }

            event.setCancelled(true);

            if (player.isInsideVehicle()) {
                player.leaveVehicle();
                return;
            }

            ArmorStand armorStand = player.getWorld().spawn(clickedBlock.getLocation().add(0.5, 0.5, 0.5), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setSmall(true);
            armorStand.addPassenger(player);
        }
    }

    private boolean isValidSitBlock(Material material) {
        String name = material.name().toLowerCase();
        
        return name.contains("stair") || 
               name.contains("slab") || 
               name.contains("carpet");
    }

    private boolean isSittingStick(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.hasDisplayName() && meta.getDisplayName().equals("§6Sitting Stick");
    }

    private String blockLocationToString(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private void activateChairParticles(Player player) {
        UUID playerId = player.getUniqueId();

        if (activeParticleEffects.containsKey(playerId)) {
            player.sendMessage("§eParticle effect is already active! Wait 3 minutes or the task will end.");
            return;
        }

        if (plugin.getMarkedChairs().isEmpty()) {
            player.sendMessage("§cNo marked chairs found!");
            return;
        }

        player.sendMessage("§aParticles enabled for all marked chairs for 3 minutes!");

        int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            spawnChairParticles(player);
        }, 0L, 5L);

        activeParticleEffects.put(playerId, taskId);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            deactivateChairParticles(player);
        }, 3600L);
    }

    private void spawnChairParticles(Player player) {
        for (String chairKey : plugin.getMarkedChairs()) {
            Location chairLocation = parseLocationFromString(chairKey);
            if (chairLocation != null && chairLocation.getWorld() != null) {
                Location particleLocation = chairLocation.add(0.5, 1.2, 0.5);
                player.spawnParticle(Particle.ENCHANT, particleLocation, 5, 0.2, 0.2, 0.2);
            }
        }
    }

    private void deactivateChairParticles(Player player) {
        UUID playerId = player.getUniqueId();

        if (!activeParticleEffects.containsKey(playerId)) {
            return;
        }

        int taskId = activeParticleEffects.get(playerId);
        plugin.getServer().getScheduler().cancelTask(taskId);
        activeParticleEffects.remove(playerId);

        player.sendMessage("§cParticle effect ended.");
    }

    private Location parseLocationFromString(String chairKey) {
        try {
            String[] parts = chairKey.split(":");
            if (parts.length != 4) {
                return null;
            }

            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                return null;
            }

            return new Location(world, x, y, z);
        } catch (Exception ignored) {
            return null;
        }
    }
}