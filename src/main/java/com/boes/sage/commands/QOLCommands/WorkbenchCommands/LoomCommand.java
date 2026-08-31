package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class LoomCommand {

    @Command("loom")
    @Permission("sage.loom")
    public void onCommand(Player player) {
        player.openLoom(player.getLocation(), true);
    }
}
