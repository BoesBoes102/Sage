package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class SpawnMobCommand {

    private final Sage plugin;

    public SpawnMobCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("spawnmob <entityType> <amount>")
    @Permission("sage.spawnmob")
    public void onCommand(
            Player player,
            @Argument(value = "entityType", suggestions = "entitytypes") EntityType entityType,
            @Argument(value = "amount", suggestions = "spawnAmounts") int amount
    ) {
        if (amount < 1) {
            player.sendMessage("§cAmount must be at least 1!");
            return;
        }

        if (!entityType.isSpawnable() || !entityType.isAlive()) {
            player.sendMessage("§cThis entity cannot be spawned!");
            return;
        }

        final int totalAmount = amount;
        final int batchSize = 10;
        final int[] spawned = {0};

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }

            int remaining = totalAmount - spawned[0];
            int toSpawn = Math.min(batchSize, remaining);

            for (int i = 0; i < toSpawn; i++) {
                player.getWorld().spawnEntity(player.getLocation(), entityType);
            }

            spawned[0] += toSpawn;

            if (spawned[0] >= totalAmount) {
                task.cancel();
            }
        }, 0L, 1L);

        player.sendMessage("§aSpawned §e" + amount + " §a" + entityType.name().toLowerCase() + "(s)!");
    }
}
