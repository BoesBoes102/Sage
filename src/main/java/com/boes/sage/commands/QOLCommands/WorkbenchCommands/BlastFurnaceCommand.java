package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class BlastFurnaceCommand {

    private final Sage plugin;

    public BlastFurnaceCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("blastfurnace")
    @Permission("sage.blastfurnace")
    public void onCommand(Player player) {
        plugin.getTempWorkbenchManager().open(player, Material.BLAST_FURNACE, "blast furnace");
    }
}
