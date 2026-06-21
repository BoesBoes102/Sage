package com.boes.sage.features.punishment.listeners;

import com.boes.sage.Sage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PunishmentChatListener implements Listener {
    private final Sage plugin;

    public PunishmentChatListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getPunishmentService().isMuted(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("Â§cYou are muted and cannot chat!");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getPunishmentService().isMuted(event.getPlayer().getUniqueId())) {
            return;
        }

        String command = event.getMessage().toLowerCase().split(" ")[0];
        if (command.equals("/msg") || command.equals("/tell") || command.equals("/pm")
                || command.equals("/w") || command.equals("/whisper") || command.equals("/reply")
                || command.equals("/r")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Â§cYou are muted and cannot send private messages!");
        }
    }
}
