package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class SmokerCommand {

    private final Sage plugin;

    public SmokerCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("smoker")
    @Permission("sage.smoker")
    public void onCommand(Player player) {
        plugin.getTempWorkbenchManager().open(player, Material.SMOKER, "smoker");
    }
}
