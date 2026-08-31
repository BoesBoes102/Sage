package com.boes.sage.features.openinv.commands;

import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import com.boes.sage.features.openinv.OpenInventoryService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.UUID;

public class OpenInventoryCommand {
    private final Sage plugin;
    private final OpenInventoryService service;

    public OpenInventoryCommand(Sage plugin) {
        this.plugin = plugin;
        this.service = plugin.getOpenInventoryService();
    }

    @Command("openinv <player>")
    @Command("invsee <player>")
    @Command("viewinv <player>")
    @Permission("sage.openinv")
    public void onCommand(Player viewer, @Argument(value = "player", suggestions = "players") String targetNameArg) {
        if (targetNameArg == null) {
            viewer.sendMessage(ChatColor.RED + "Usage: /openinv <player>");
            return;
        }

        Player onlineTarget = Bukkit.getPlayer(targetNameArg);

        if (onlineTarget != null) {
            UUID targetUUID = onlineTarget.getUniqueId();
            String targetName = onlineTarget.getName();
            openFor(viewer, targetUUID, targetName);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID targetUUID = OfflinePlayerDataManager.getPlayerUUID(targetNameArg);
            if (targetUUID == null) {
                Bukkit.getScheduler().runTask(plugin, () -> viewer.sendMessage(ChatColor.RED + "Player not found!"));
                return;
            }

            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetUUID);
            String targetName = offlineTarget.getName() != null ? offlineTarget.getName() : targetNameArg;

            Bukkit.getScheduler().runTask(plugin, () -> openFor(viewer, targetUUID, targetName));
        });
    }

    private void openFor(Player viewer, UUID targetUUID, String targetName) {
        if (!viewer.isOnline()) {
            return;
        }

        if (targetUUID.equals(viewer.getUniqueId())) {
            viewer.sendMessage(ChatColor.RED + "You cannot open your own inventory!");
            return;
        }

        try {
            service.openInventory(viewer, targetUUID, targetName);
            viewer.sendMessage(ChatColor.GREEN + "Opened inventory of " + ChatColor.YELLOW + targetName);
        } catch (Exception e) {
            viewer.sendMessage(ChatColor.RED + "Error opening inventory!");
            e.printStackTrace();
        }
    }
}
