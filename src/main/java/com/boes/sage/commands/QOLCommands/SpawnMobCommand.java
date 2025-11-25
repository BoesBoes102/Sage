package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@CommandAlias("spawnmob")
@Description("Spawn mobs at your location")
@CommandPermission("sage.spawnmob")
public class SpawnMobCommand extends BaseCommand {

    private final Sage plugin;

    public SpawnMobCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @CommandCompletion("@entitytypes 1|5|10|20")
    public void onCommand(Player player, EntityType entityType, @Optional Integer amount) {
        if (amount == null) {
            amount = 1;
        }

        if (amount < 1) {
            player.sendMessage("§cAmount must be at least 1!");
            return;
        }

        if (!entityType.isSpawnable() || !entityType.isAlive()) {
            player.sendMessage("§cThis entity cannot be spawned!");
            return;
        }

        for (int i = 0; i < amount; i++) {
            player.getWorld().spawnEntity(player.getLocation(), entityType);
        }

        player.sendMessage("§aSpawned §e" + amount + " §a" + entityType.name().toLowerCase() + "(s)!");
    }
}