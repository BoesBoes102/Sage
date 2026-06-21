package com.boes.sage.features.spy.listeners;

import com.boes.sage.Sage;
import com.boes.sage.features.spy.SpyService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleSpyListener extends Handler {
    private final Sage plugin;

    public ConsoleSpyListener(Sage plugin) {
        this.plugin = plugin;
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null || record.getMessage() == null) {
            return;
        }

        if (!plugin.isEnabled()) {
            return;
        }

        String message = record.getMessage();

        SpyService spyManager = plugin.getSpyService();
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID uuid : spyManager.getConsoleSpyPlayers()) {
                Player spy = Bukkit.getPlayer(uuid);
                if (spy != null && spy.isOnline()) {
                    spy.sendMessage("§8[CONSOLE] §7" + message);
                }
            }
        });
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    public void cleanup() {
        try {
            close();
        } catch (SecurityException e) {
            plugin.getLogger().severe("Failed to close ConsoleSpyListener: " + e.getMessage());
        }
    }
}
