package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class CraftingTableCommand {

    @Command("craft")
    @Command("workbench")
    @Command("craftingtable")
    @Permission("sage.craft")
    public void onCommand(Player player) {
        player.openWorkbench(player.getLocation(), true);
    }
}
