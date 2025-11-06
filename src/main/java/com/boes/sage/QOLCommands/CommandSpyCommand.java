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

@CommandAlias("commandspy")
@Description("Toggle command spy mode")
public class CommandSpyCommand extends BaseCommand {

    private final Sage plugin;

    public CommandSpyCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("on|off")
    @Conditions("permission:sage.commandspy")
    public void onCommand(Player player, @Optional String state) {
        SpyManager spyManager = plugin.getSpyManager();

        if (state == null) {
            boolean newState = spyManager.toggleCommandSpy(player);
            player.sendMessage(newState ? "§aCommand spy enabled!" : "§cCommand spy disabled!");
        } else {
            state = state.toLowerCase();
            if (state.equals("on") || state.equals("true")) {
                spyManager.setCommandSpy(player, true);
                player.sendMessage("§aCommand spy enabled!");
            } else if (state.equals("off") || state.equals("false")) {
                spyManager.setCommandSpy(player, false);
                player.sendMessage("§cCommand spy disabled!");
            } else {
                player.sendMessage("§cUsage: /commandspy <on/off>");
            }
        }
    }
}