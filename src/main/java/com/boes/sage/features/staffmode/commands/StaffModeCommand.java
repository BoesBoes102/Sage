package com.boes.sage.features.staffmode.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.features.staffmode.StaffModeService;
import org.bukkit.entity.Player;

@CommandAlias("staffmode|h")
@CommandPermission("sage.staffmode")
public class StaffModeCommand extends BaseCommand {
    private final Sage plugin;

    public StaffModeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("")
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
