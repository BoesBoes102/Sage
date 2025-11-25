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

@CommandAlias("commandspy|spycmd")
@Description("Toggle command spy mode")
@CommandPermission("sage.commandspy")
public class CommandSpyCommand extends BaseCommand implements Listener {

    private final Sage plugin;

    public CommandSpyCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @CommandCompletion("on|off")
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SpyManager spyManager = plugin.getSpyManager();

        if (spyManager.hasCommandSpy(player) && !player.hasPermission("sage.commandspy")) {
            spyManager.setCommandSpy(player, false);
        }
    }
}