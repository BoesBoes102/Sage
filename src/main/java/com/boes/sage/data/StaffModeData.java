package com.boes.sage.data;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class StaffModeData {
    private final Location location;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final GameMode gameMode;

    public StaffModeData(Location location, ItemStack[] inventory, ItemStack[] armor, GameMode gameMode) {
        this.location = location;
        this.inventory = inventory;
        this.armor = armor;
        this.gameMode = gameMode;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}