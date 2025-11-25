package com.boes.sage.commands.TeleportCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import com.boes.sage.gui.WorldGUI;
import org.bukkit.entity.Player;

@CommandAlias("world")
@CommandPermission("sage.world")
public class WorldCommand extends BaseCommand {

    private final Sage plugin;

    public WorldCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        WorldGUI worldGUI = new WorldGUI(player);
        worldGUI.open();
    }
}