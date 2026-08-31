package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class FurnaceCommand {

    private final Sage plugin;

    public FurnaceCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("furnace")
    @Permission("sage.furnace")
    public void onCommand(Player player) {
        plugin.getTempWorkbenchManager().open(player, Material.FURNACE, "furnace");
    }
}
