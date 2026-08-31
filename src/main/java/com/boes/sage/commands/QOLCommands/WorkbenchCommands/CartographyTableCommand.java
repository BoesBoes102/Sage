package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class CartographyTableCommand {

    @Command("cartographytable")
    @Command("cartography")
    @Permission("sage.cartographytable")
    public void onCommand(Player player) {
        player.openCartographyTable(player.getLocation(), true);
    }
}
