package com.boes.sage.data;

import org.bukkit.Location;

public class Warp {

    private final String name;
    private Location location;
    private boolean hidden;

    public Warp(String name, Location location, boolean hidden) {
        this.name = name;
        this.location = location.clone();
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location.clone();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getWorldName() {
        return location.getWorld() != null ? location.getWorld().getName() : null;
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

    public float getYaw() {
        return location.getYaw();
    }

    public float getPitch() {
        return location.getPitch();
    }
}