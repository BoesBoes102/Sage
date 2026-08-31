package com.boes.sage.commands.QOLCommands.DisposeCommand;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class DisposeCommand {

    private final Sage plugin;

    public DisposeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("dispose")
    @Command("thrash")
    @Permission("sage.dispose")
    public void onCommand(Player player) {
        DisposeGUI gui = new DisposeGUI(player);
        gui.open();
    }
}
