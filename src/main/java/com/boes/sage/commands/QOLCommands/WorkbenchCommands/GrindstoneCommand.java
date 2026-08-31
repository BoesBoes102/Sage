package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class GrindstoneCommand {

    @Command("grindstone")
    @Permission("sage.grindstone")
    public void onCommand(Player player) {
        player.openGrindstone(player.getLocation(), true);
    }
}
