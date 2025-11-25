package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import com.boes.sage.gui.DisposeGUI;
import org.bukkit.entity.Player;

@CommandAlias("dispose|thrash")
@Description("Open the item disposal GUI")
@CommandPermission("sage.dispose")
public class DisposeCommand extends BaseCommand {

    private final Sage plugin;

    public DisposeCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    public void onCommand(Player player) {
        DisposeGUI gui = new DisposeGUI(player);
        gui.open();
    }
}