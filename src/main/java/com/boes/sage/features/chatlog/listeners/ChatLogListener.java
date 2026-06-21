package com.boes.sage.features.chatlog.listeners;

import com.boes.sage.Sage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatLogListener implements Listener {
    private final Sage plugin;

    public ChatLogListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        plugin.getChatLogService().logMessage(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                event.getMessage()
        );
    }
}
