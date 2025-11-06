package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import com.boes.sage.managers.SpyManager;
import org.bukkit.entity.Player;

@CommandAlias("consolespy")
@Description("Toggle console spy mode")
public class ConsoleSpyCommand extends BaseCommand {

    private final Sage plugin;

    public ConsoleSpyCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("on|off")
    @Conditions("permission:sage.consolespy")
    public void onCommand(Player player, @Optional String state) {
        SpyManager spyManager = plugin.getSpyManager();

        if (state == null) {
            boolean newState = spyManager.toggleConsoleSpy(player);
            player.sendMessage(newState ? "§aConsole spy enabled!" : "§cConsole spy disabled!");
        } else {
            state = state.toLowerCase();
            if (state.equals("on") || state.equals("true")) {
                spyManager.setConsoleSpy(player, true);
                player.sendMessage("§aConsole spy enabled!");
            } else if (state.equals("off") || state.equals("false")) {
                spyManager.setConsoleSpy(player, false);
                player.sendMessage("§cConsole spy disabled!");
            } else {
                player.sendMessage("§cUsage: /consolespy <on/off>");
            }
        }
    }
}