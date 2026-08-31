package com.boes.sage.commands.TeleportCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldCommand {

    private static final String RED = "§c";
    private static final String YELLOW = "§e";
    private static final String GREEN = "§a";
    private static final String GOLD = "§6";
    private static final String GRAY = "§7";
    private static final String WHITE = "§f";

    private final Sage plugin;

    public WorldCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("world [world]")
    @Permission("sage.world")
    public void onCommand(Player player, @Argument(value = "world", suggestions = "worldNames") String worldName) {
        if (worldName == null || worldName.isBlank()) {
            sendWorldList(player);
            return;
        }

        World world = Bukkit.getWorld(worldName);
        boolean alreadyLoaded = world != null;

        if (!alreadyLoaded) {
            String matchedWorldName = findWorldName(worldName);
            if (matchedWorldName == null) {
                player.sendMessage(RED + "World not found: " + YELLOW + worldName);
                sendWorldList(player);
                return;
            }

            world = new WorldCreator(matchedWorldName).createWorld();
            if (world == null) {
                player.sendMessage(RED + "Failed to load world: " + YELLOW + matchedWorldName);
                return;
            }
        }

        player.teleport(resolveTeleportLocation(world));
        player.sendMessage(GREEN + "Teleported to " + YELLOW + world.getName() + GREEN
            + " (" + (alreadyLoaded ? "loaded" : "loaded now") + ")");
    }

    private void sendWorldList(Player player) {
        List<String> loadedWorlds = Bukkit.getWorlds().stream()
            .map(World::getName)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        List<String> allWorlds = getAvailableWorldNames();
        List<String> unloadedWorlds = allWorlds.stream()
            .filter(name -> loadedWorlds.stream().noneMatch(loaded -> loaded.equalsIgnoreCase(name)))
            .toList();

        player.sendMessage(GOLD + "Available worlds:");
        player.sendMessage(GREEN + "Loaded: " + formatWorldList(loadedWorlds, RED + "None"));
        player.sendMessage(YELLOW + "Unloaded: " + formatWorldList(unloadedWorlds, GRAY + "None"));
        player.sendMessage(GRAY + "Use " + WHITE + "/world <name>" + GRAY + " to teleport.");
    }

    private String formatWorldList(List<String> worldNames, String emptyValue) {
        if (worldNames.isEmpty()) {
            return emptyValue;
        }

        return WHITE + String.join(GRAY + ", " + WHITE, worldNames);
    }

    private List<String> getAvailableWorldNames() {
        List<String> worldNames = new ArrayList<>();
        File[] folders = plugin.getServer().getWorldContainer().listFiles();
        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory() && new File(folder, "level.dat").exists()) {
                    worldNames.add(folder.getName());
                }
            }
        }

        Bukkit.getWorlds().stream()
            .map(World::getName)
            .filter(name -> worldNames.stream().noneMatch(existing -> existing.equalsIgnoreCase(name)))
            .forEach(worldNames::add);

        worldNames.sort(String.CASE_INSENSITIVE_ORDER);
        return worldNames;
    }

    private String findWorldName(String input) {
        return getAvailableWorldNames().stream()
            .filter(name -> name.equalsIgnoreCase(input))
            .findFirst()
            .orElse(null);
    }

    private Location resolveTeleportLocation(World world) {
        Location spawnLocation = world.getSpawnLocation();
        if (spawnLocation != null) {
            return spawnLocation;
        }

        int highestY = world.getHighestBlockYAt(0, 0) + 1;
        return new Location(world, 0.5, highestY, 0.5);
    }
}
