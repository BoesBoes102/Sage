package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import com.boes.sage.managers.SpyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@CommandAlias("consolespy|spyco")
@Description("Toggle console spy mode")
@CommandPermission("sage.consolespy")
public class ConsoleSpyCommand extends BaseCommand implements Listener {

    private final Sage plugin;

    public ConsoleSpyCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @CommandCompletion("on|off")
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SpyManager spyManager = plugin.getSpyManager();

        if (spyManager.hasConsoleSpy(player) && !player.hasPermission("sage.consolespy")) {
            spyManager.setConsoleSpy(player, false);
        }
    }
}