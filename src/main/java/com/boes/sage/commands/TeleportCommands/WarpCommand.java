package com.boes.sage.commands.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.boes.sage.Sage;
import com.boes.sage.data.Warp;
import com.boes.sage.managers.WarpManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@CommandAlias("warp")
public class WarpCommand extends BaseCommand {

    private final WarpManager warpManager;

    private static final String ADMIN_PERMISSION = "sage.warp.admin";
    private static final String USE_PERMISSION = "sage.warp";

    public WarpCommand(Sage plugin) {
        this.warpManager = plugin.getWarpManager();
    }

    @Default
    @CommandCompletion("@warp")
    public void onWarp(Player player, @co.aikar.commands.annotation.Optional String warpName) {
        boolean hasAdmin = player.hasPermission(ADMIN_PERMISSION);

        if (!player.hasPermission(USE_PERMISSION) && !hasAdmin) {
            player.sendMessage("§cYou don't have permission!");
            return;
        }

        if (warpName == null) {
            listWarps(player, hasAdmin);
            return;
        }

        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        if (warp.isHidden() && !hasAdmin) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        Location location = warp.getLocation();
        if (location.getWorld() == null) {
            player.sendMessage("§cWarp world is not loaded!");
            return;
        }

        player.teleport(location);
        player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendMessage("§aTeleported to warp §e" + warp.getName() + "§a.");
    }

    private void listWarps(Player player, boolean includeHidden) {
        java.util.List<Warp> warps = includeHidden ? new java.util.ArrayList<>(warpManager.getWarps()) : warpManager.getVisibleWarps();
        
        if (warps.isEmpty()) {
            player.sendMessage("§cNo warps available!");
            return;
        }

        player.sendMessage("§e§l=== Available Warps ===");
        for (Warp warp : warps) {
            player.sendMessage("§7- §e" + warp.getName());
        }
    }

    @Subcommand("create")
    @CommandPermission(ADMIN_PERMISSION)
    public void onCreate(Player player, String warpName) {
        if (warpManager.warpExists(warpName)) {
            player.sendMessage("§cA warp with that name already exists!");
            return;
        }

        warpManager.createWarp(warpName, player.getLocation());
        player.sendMessage("§aWarp §e" + warpName + " §ahas been created at your location.");
    }

    @Subcommand("delete")
    @CommandPermission(ADMIN_PERMISSION)
    @CommandCompletion("@warp")
    public void onDelete(Player player, String warpName) {
        if (!warpManager.deleteWarp(warpName)) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        player.sendMessage("§aWarp §e" + warpName + " §ahas been deleted.");
    }

    @Subcommand("setlocation")
    @CommandPermission(ADMIN_PERMISSION)
    @CommandCompletion("@warp")
    public void onSetLocation(Player player, String warpName) {
        if (!warpManager.warpExists(warpName)) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        warpManager.setWarpLocation(warpName, player.getLocation());
        player.sendMessage("§aWarp §e" + warpName + " §alocation updated to your current position.");
    }

    @Subcommand("sethidden")
    @CommandPermission(ADMIN_PERMISSION)
    @CommandCompletion("@warp true|false")
    public void onSetHidden(Player player, String warpName, boolean hidden) {
        if (!warpManager.warpExists(warpName)) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        warpManager.setHidden(warpName, hidden);
        player.sendMessage("§aWarp §e" + warpName + " §ais now §e" + (hidden ? "hidden" : "visible") + "§a.");
    }
}