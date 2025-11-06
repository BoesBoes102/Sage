package com.boes.sage.listeners;

import com.boes.sage.Sage;
import com.boes.sage.managers.SpyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class CommandSpyListener implements Listener {
    private final Sage plugin;

    public CommandSpyListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        
        SpyManager spyManager = plugin.getSpyManager();
        
        for (UUID uuid : spyManager.getCommandSpyPlayers()) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null && spy.isOnline() && !spy.equals(player)) {
                spy.sendMessage("§7[CMD] §e" + player.getName() + "§7: §f" + command);
            }
        }

        plugin.getChatLogManager().logCommand(player.getUniqueId(), player.getName(), command);
    }
}