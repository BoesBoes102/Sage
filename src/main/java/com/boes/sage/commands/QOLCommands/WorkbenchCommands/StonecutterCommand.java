package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class StonecutterCommand {

    @Command("stonecutter")
    @Permission("sage.stonecutter")
    public void onCommand(Player player) {
        player.openStonecutter(player.getLocation(), true);
    }
}
