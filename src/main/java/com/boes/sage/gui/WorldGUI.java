package com.boes.sage.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class WorldGUI {
    private final Player player;
    private final Inventory inventory;

    public WorldGUI(Player player) {
        this.player = player;
        
        List<World> worlds = Bukkit.getWorlds();
        int size = Math.min(54, ((worlds.size() + 8) / 9) * 9);
        if (size < 9) size = 9;
        
        this.inventory = Bukkit.createInventory(null, size, "§6§lSelect World");
    }

    public void open() {
        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        List<World> worlds = Bukkit.getWorlds();
        
        for (int i = 0; i < worlds.size() && i < inventory.getSize(); i++) {
            World world = worlds.get(i);
            ItemStack worldItem = new ItemStack(getWorldMaterial(world));
            ItemMeta meta = worldItem.getItemMeta();
            
            meta.setDisplayName("§e§l" + world.getName());
            meta.setLore(Arrays.asList(
                "",
                "§7Environment: §f" + world.getEnvironment().name(),
                "§7Players: §f" + world.getPlayers().size(),
                "§7Difficulty: §f" + world.getDifficulty().name(),
                "",
                "§aClick to teleport!"
            ));
            
            worldItem.setItemMeta(meta);
            inventory.setItem(i, worldItem);
        }
    }

    private Material getWorldMaterial(World world) {
        return switch (world.getEnvironment()) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.GRASS_BLOCK;
        };
    }

    public Inventory getInventory() {
        return inventory;
    }
}