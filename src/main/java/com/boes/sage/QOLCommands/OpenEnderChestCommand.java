package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("openender")
@Description("Open another player's ender chest")
public class OpenEnderChestCommand extends BaseCommand implements Listener {
    private final Sage plugin;
    private final Map<UUID, String> openOfflineEnderChests = new HashMap<>();

    public OpenEnderChestCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.openender")
    public void onCommand(Player player, Player target) {
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot open your own ender chest!");
            return;
        }

        try {
            player.openInventory(target.getEnderChest());
            player.sendMessage("§aOpened ender chest of §e" + target.getName());
        } catch (Exception e) {
            player.sendMessage("§cError opening ender chest!");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        UUID playerUuid = player.getUniqueId();
        
        if (openOfflineEnderChests.containsKey(playerUuid)) {
            String targetName = openOfflineEnderChests.get(playerUuid);
            Inventory closedInv = event.getInventory();
            
            OfflinePlayerDataManager.saveOfflinePlayerData(
                OfflinePlayerDataManager.getPlayerUUID(targetName),
                closedInv,
                "ENDER_CHEST"
            );
            
            openOfflineEnderChests.remove(playerUuid);
        }
    }
}