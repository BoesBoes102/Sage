package com.boes.sage.features.staffmode.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.staffmode.StaffModeService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class StaffModeCommand {
    private final Sage plugin;

    public StaffModeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("staffmode")
    @Command("h")
    @Permission("sage.staffmode")
    public void onCommand(Player player) {
        StaffModeService manager = plugin.getStaffModeService();

        if (manager.isInStaffMode(player)) {
            manager.disableStaffMode(player);
            player.sendMessage("§aStaff mode disabled!");
        } else {
            manager.enableStaffMode(player);
            player.sendMessage("§aStaff mode enabled!");
        }
    }
}
