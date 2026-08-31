package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class SmithingTableCommand {

    @Command("smithingtable")
    @Command("smith")
    @Permission("sage.smithingtable")
    public void onCommand(Player player) {
        player.openSmithingTable(player.getLocation(), true);
    }
}
