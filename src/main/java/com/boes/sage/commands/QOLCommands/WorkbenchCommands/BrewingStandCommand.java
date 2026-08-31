package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class BrewingStandCommand {

    private final Sage plugin;

    public BrewingStandCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("brewingstand")
    @Permission("sage.brewingstand")
    public void onCommand(Player player) {
        plugin.getTempWorkbenchManager().open(player, Material.BREWING_STAND, "brewing stand");
    }
}
