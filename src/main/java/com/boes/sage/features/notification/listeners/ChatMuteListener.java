package com.boes.sage.features.notification.listeners;

import com.boes.sage.features.notification.commands.MuteChatCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatMuteListener implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!MuteChatCommand.isChatMuted()) {
            return;
        }

        if (event.getPlayer().hasPermission("sage.mutechat.bypass")) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("Â§cChat is currently muted!");
    }
}
