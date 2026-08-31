package com.boes.sage.features.warp.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.warp.data.Warp;
import com.boes.sage.features.warp.WarpService;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class WarpCommand {

    private final WarpService warpManager;

    private static final String ADMIN_PERMISSION = "sage.warp.admin";
    private static final String USE_PERMISSION = "sage.warp";

    public WarpCommand(Sage plugin) {
        this.warpManager = plugin.getWarpService();
    }

    @Command("warp [warp]")
    public void onWarp(Player player, @Argument(value = "warp", suggestions = "warp") String warpName) {
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

    @Command("warp create <name>")
    @Permission(ADMIN_PERMISSION)
    public void onCreate(Player player, @Argument("name") String warpName) {
        if (warpManager.warpExists(warpName)) {
            player.sendMessage("§cA warp with that name already exists!");
            return;
        }

        warpManager.createWarp(warpName, player.getLocation());
        player.sendMessage("§aWarp §e" + warpName + " §ahas been created at your location.");
    }

    @Command("warp delete <name>")
    @Permission(ADMIN_PERMISSION)
    public void onDelete(Player player, @Argument(value = "name", suggestions = "warpAdmin") String warpName) {
        if (!warpManager.deleteWarp(warpName)) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        player.sendMessage("§aWarp §e" + warpName + " §ahas been deleted.");
    }

    @Command("warp setlocation <name>")
    @Permission(ADMIN_PERMISSION)
    public void onSetLocation(Player player, @Argument(value = "name", suggestions = "warpAdmin") String warpName) {
        if (!warpManager.warpExists(warpName)) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        warpManager.setWarpLocation(warpName, player.getLocation());
        player.sendMessage("§aWarp §e" + warpName + " §alocation updated to your current position.");
    }

    @Command("warp sethidden <name> <hidden>")
    @Permission(ADMIN_PERMISSION)
    public void onSetHidden(
            Player player,
            @Argument(value = "name", suggestions = "warpAdmin") String warpName,
            @Argument("hidden") boolean hidden
    ) {
        if (!warpManager.warpExists(warpName)) {
            player.sendMessage("§cWarp not found!");
            return;
        }

        warpManager.setHidden(warpName, hidden);
        player.sendMessage("§aWarp §e" + warpName + " §ais now §e" + (hidden ? "hidden" : "visible") + "§a.");
    }
}
