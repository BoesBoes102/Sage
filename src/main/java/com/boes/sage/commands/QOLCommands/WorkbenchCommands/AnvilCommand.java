package com.boes.sage.commands.QOLCommands.WorkbenchCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class AnvilCommand {

    @Command("anvil")
    @Permission("sage.anvil")
    public void onCommand(Player player) {
        player.openAnvil(player.getLocation(), true);
    }
}
