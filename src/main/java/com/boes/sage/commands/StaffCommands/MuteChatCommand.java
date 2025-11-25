package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("mutechat")
@CommandPermission("sage.mutechat")
public class MuteChatCommand extends BaseCommand {

    private final Sage plugin;
    private static boolean chatMuted = false;

    public MuteChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(org.bukkit.command.CommandSender sender) {
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