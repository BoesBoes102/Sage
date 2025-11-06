package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import com.boes.sage.gui.DisposeGUI;
import org.bukkit.entity.Player;

@CommandAlias("dispose")
@Description("Open the item disposal GUI")
public class DisposeCommand extends BaseCommand {

    private final Sage plugin;

    public DisposeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Conditions("permission:sage.dispose")
    public void onCommand(Player player) {
        DisposeGUI gui = new DisposeGUI(player);
        gui.open();
    }
}