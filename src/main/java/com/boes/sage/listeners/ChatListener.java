package com.boes.sage.listeners;

import com.boes.sage.Sage;
import com.boes.sage.commands.StaffCommands.MuteChatCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener {
    private final Sage plugin;

    public ChatListener() {
        this.plugin = null;
    }

    public ChatListener(Sage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (MuteChatCommand.isChatMuted()) {
            if (event.getPlayer().hasPermission("sage.mutechat.bypass")) {
                return;
            }

            event.setCancelled(true);
            event.getPlayer().sendMessage("§cChat is currently muted!");
            return;
        }

        if (plugin != null && plugin.getPunishmentManager().isMuted(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou are muted and cannot chat!");
            return;
        }

        if (plugin != null) {
            plugin.getChatLogManager().logMessage(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                event.getMessage()
            );
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (plugin == null || !plugin.getPunishmentManager().isMuted(event.getPlayer().getUniqueId())) {
            return;
        }

        String command = event.getMessage().toLowerCase().split(" ")[0];

        if (command.equals("/msg") || command.equals("/tell") || command.equals("/pm") || 
            command.equals("/w") || command.equals("/whisper") || command.equals("/reply") || 
            command.equals("/r")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou are muted and cannot send private messages!");
        }
    }
}