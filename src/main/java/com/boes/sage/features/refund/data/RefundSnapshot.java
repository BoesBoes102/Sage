package com.boes.sage.features.refund.data;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class RefundSnapshot {
    private final String id;
    private final UUID playerUuid;
    private final String playerName;
    private final long createdAt;
    private final long expiresAt;
    private final String saveReason;
    private final String deathReason;
    private final Location location;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final ItemStack offhand;
    private final int totalExperience;
    private final int level;
    private final float expProgress;
    private final boolean pendingClaim;

    public RefundSnapshot(String id, UUID playerUuid, String playerName, long createdAt, long expiresAt,
                          String saveReason, String deathReason, Location location, ItemStack[] inventory,
                          ItemStack[] armor, ItemStack offhand, int totalExperience, int level,
                          float expProgress, boolean pendingClaim) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.saveReason = saveReason;
        this.deathReason = deathReason;
        this.location = location;
        this.inventory = inventory;
        this.armor = armor;
        this.offhand = offhand;
        this.totalExperience = totalExperience;
        this.level = level;
        this.expProgress = expProgress;
        this.pendingClaim = pendingClaim;
    }

    public String getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public String getSaveReason() {
        return saveReason;
    }

    public String getDeathReason() {
        return deathReason;
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

    public ItemStack getOffhand() {
        return offhand;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public int getLevel() {
        return level;
    }

    public float getExpProgress() {
        return expProgress;
    }

    public boolean isPendingClaim() {
        return pendingClaim;
    }
}
