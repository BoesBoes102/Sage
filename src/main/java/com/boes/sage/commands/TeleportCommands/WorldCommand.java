package com.boes.sage.commands.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandAlias("world")
@CommandPermission("sage.world")
public class WorldCommand extends BaseCommand {

    private static final String RED = "\u00A7c";
    private static final String YELLOW = "\u00A7e";
    private static final String GREEN = "\u00A7a";
    private static final String GOLD = "\u00A76";
    private static final String GRAY = "\u00A77";
    private static final String WHITE = "\u00A7f";

    private final Sage plugin;

    public WorldCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("[world]")
    @CommandCompletion("@worldNames")
    public void onCommand(Player player, @Optional @Single String worldName) {
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
