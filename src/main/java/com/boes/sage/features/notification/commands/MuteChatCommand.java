package com.boes.sage.features.notification.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class MuteChatCommand {

    private final Sage plugin;
    private static boolean chatMuted = false;

    public MuteChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("mutechat")
    @Permission("sage.mutechat")
    public void onCommand(CommandSender sender) {
        chatMuted = !chatMuted;
        String status = chatMuted ? "§a§lMUTED" : "§c§lUNMUTED";
        String broadcastMsg = chatMuted ? "§cChat has been muted by " + sender.getName() + "!" : "§aChat has been unmuted!";

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.sendMessage("§6" + broadcastMsg);
            player.sendMessage("");
        }
    }

    public static boolean isChatMuted() {
        return chatMuted;
    }
}
