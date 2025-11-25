package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
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

@CommandAlias("openender|endersee|ec|viewender")
@Description("Open another player's ender chest")
@CommandPermission("sage.openender")
public class OpenEnderChestCommand extends BaseCommand implements Listener {
    private final Sage plugin;
    private final Map<UUID, String> openOfflineEnderChests = new HashMap<>();

    public OpenEnderChestCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, String[] args) {
        Player target = null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                String playerName = args[0];
                UUID targetUUID = OfflinePlayerDataManager.getPlayerUUID(playerName);
                
                if (targetUUID == null) {
                    player.sendMessage("§cPlayer not found!");
                    return;
                }
                
                try {
                    Inventory offlineEnderChest = OfflinePlayerDataManager.getOfflinePlayerEnderChest(playerName);
                    if (offlineEnderChest == null) {
                        player.sendMessage("§cNo ender chest data found for offline player §e" + playerName);
                        return;
                    }
                    
                    player.openInventory(offlineEnderChest);
                    openOfflineEnderChests.put(player.getUniqueId(), playerName);
                    player.sendMessage("§aOpened ender chest of offline player §e" + playerName);
                } catch (Exception e) {
                    player.sendMessage("§cError opening ender chest!");
                    e.printStackTrace();
                }
                return;
            }
        }

        if (target == null) {
            try {
                player.openInventory(player.getEnderChest());
                player.sendMessage("§aOpened your ender chest");
            } catch (Exception e) {
                player.sendMessage("§cError opening ender chest!");
                e.printStackTrace();
            }
            return;
        }

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