package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Backs /furnace, /blastfurnace, /smoker and /brewingstand with a real, temporarily
 * placed block so fuel/brewing actually ticks, then reverts the block and hands
 * back any leftover items once the player closes the menu.
 */
public class TempWorkbenchManager implements Listener {
    private final Sage plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public TempWorkbenchManager(Sage plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Material workbenchMaterial, String displayName) {
        if (sessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have a workbench open!");
            return;
        }

        Location location = findOpenAirAbove(player);
        if (location == null) {
            player.sendMessage(ChatColor.RED + "Unable to open the " + displayName + " here, try a different location.");
            return;
        }

        BlockData originalData = location.getBlock().getBlockData().clone();
        location.getBlock().setType(workbenchMaterial, false);

        BlockState state = location.getBlock().getState();
        Inventory inventory;
        if (state instanceof Furnace furnace) {
            inventory = furnace.getInventory();
        } else if (state instanceof BrewingStand brewingStand) {
            inventory = brewingStand.getInventory();
        } else {
            location.getBlock().setBlockData(originalData, false);
            player.sendMessage(ChatColor.RED + "Failed to open the " + displayName + "!");
            return;
        }

        sessions.put(player.getUniqueId(), new Session(location, originalData, inventory));
        player.openInventory(inventory);
    }

    private Location findOpenAirAbove(Player player) {
        World world = player.getWorld();
        int x = player.getLocation().getBlockX();
        int z = player.getLocation().getBlockZ();
        int maxY = world.getMaxHeight() - 1;
        int minSearchY = Math.max(player.getLocation().getBlockY() + 3, maxY - 8);

        for (int y = maxY; y >= minSearchY; y--) {
            Location candidate = new Location(world, x, y, z);
            if (candidate.getBlock().getType() == Material.AIR) {
                return candidate;
            }
        }

        return null;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Session session = sessions.get(player.getUniqueId());
        if (session == null || !session.inventory.equals(event.getInventory())) return;

        endSession(player.getUniqueId(), session);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session != null) {
            endSession(event.getPlayer().getUniqueId(), session);
        }
    }

    private void endSession(UUID uuid, Session session) {
        sessions.remove(uuid);

        for (ItemStack item : session.inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                leftover.values().forEach(extra -> player.getWorld().dropItemNaturally(player.getLocation(), extra));
            } else {
                session.location.getWorld().dropItemNaturally(session.location, item);
            }
        }

        session.location.getBlock().setBlockData(session.originalData, false);
    }

    public void cleanup() {
        for (Map.Entry<UUID, Session> entry : new HashMap<>(sessions).entrySet()) {
            endSession(entry.getKey(), entry.getValue());
        }
    }

    private static class Session {
        private final Location location;
        private final BlockData originalData;
        private final Inventory inventory;

        private Session(Location location, BlockData originalData, Inventory inventory) {
            this.location = location;
            this.originalData = originalData;
            this.inventory = inventory;
        }
    }
}
