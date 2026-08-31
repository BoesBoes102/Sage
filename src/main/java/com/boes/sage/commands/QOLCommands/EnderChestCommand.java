package com.boes.sage.commands.QOLCommands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class EnderChestCommand {

    @Command("ec")
    @Command("enderchest")
    @Permission("sage.enderchest")
    public void onCommand(Player player) {
        player.openInventory(player.getEnderChest());
    }
}
