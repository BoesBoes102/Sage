package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import com.boes.sage.managers.StaffModeManager;
import org.bukkit.entity.Player;

@CommandAlias("staffmode")
@CommandPermission("sage.staffmode")
public class StaffModeCommand extends BaseCommand {
    private final Sage plugin;

    public StaffModeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        StaffModeManager manager = plugin.getStaffModeManager();

        if (manager.isInStaffMode(player)) {
            manager.disableStaffMode(player);
            player.sendMessage("§aStaff mode disabled!");
        } else {
            manager.enableStaffMode(player);
            player.sendMessage("§aStaff mode enabled!");
        }
    }
}