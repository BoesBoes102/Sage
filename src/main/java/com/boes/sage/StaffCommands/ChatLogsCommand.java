package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import com.boes.sage.data.ChatLogEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CommandAlias("chatlogs")
@CommandPermission("sage.chatlogs")
public class ChatLogsCommand extends BaseCommand {
    private final Sage plugin;

    public ChatLogsCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("message|command @players")
    public void onCommand(Player player, String type, String playerName, @Optional Integer page) {
        if (!type.equalsIgnoreCase("message") && !type.equalsIgnoreCase("command")) {
            player.sendMessage("§cType must be 'message' or 'command'!");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        int pageNum = page != null ? page : 1;
        if (pageNum < 1) {
            player.sendMessage("§cPage number must be at least 1!");
            return;
        }

        List<ChatLogEntry> logs = plugin.getChatLogManager().getLogsPage(target.getUniqueId(), type, pageNum);
        int totalPages = plugin.getChatLogManager().getTotalPages(target.getUniqueId(), type);

        if (logs.isEmpty()) {
            player.sendMessage("§cNo " + type + " logs found for " + target.getName() + "!");
            return;
        }

        player.sendMessage("§e========== " + target.getName() + "'s " + type.toUpperCase() + " Logs ==========");
        player.sendMessage("§7Page " + pageNum + " of " + totalPages);
        player.sendMessage("");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (ChatLogEntry log : logs) {
            String date = sdf.format(new Date(log.getTimestamp()));
            player.sendMessage("§7[§f" + date + "§7] §f" + log.getMessage());
        }

        player.sendMessage("");
        
        if (totalPages > 1) {
            StringBuilder pagination = new StringBuilder("§7Navigation: ");
            if (pageNum > 1) {
                pagination.append("§e[<< PREVIOUS]§7 ");
            }
            pagination.append("§f[§e").append(pageNum).append("§f/§e").append(totalPages).append("§f]");
            if (pageNum < totalPages) {
                pagination.append(" §e[NEXT >>]");
            }
            player.sendMessage(pagination.toString());
            StringBuilder usage = new StringBuilder("§7Use: /chatlogs ").append(type).append(" ").append(target.getName()).append(" <page>");
            player.sendMessage(usage.toString());
        }
    }
}