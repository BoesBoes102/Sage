package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import com.boes.sage.data.ChatLogEntry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
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
            TextComponent navigationComponent = new TextComponent("§7Navigation: ");
            
            if (pageNum > 1) {
                TextComponent previousComponent = new TextComponent("§e[<< PREVIOUS]");
                previousComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                    "/chatlogs " + type + " " + target.getName() + " " + (pageNum - 1)));
                navigationComponent.addExtra(previousComponent);
                navigationComponent.addExtra(new TextComponent("§7 "));
            }
            
            navigationComponent.addExtra(new TextComponent("§f[§e" + pageNum + "§f/§e" + totalPages + "§f]"));
            
            if (pageNum < totalPages) {
                TextComponent nextComponent = new TextComponent(" §e[NEXT >>]");
                nextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                    "/chatlogs " + type + " " + target.getName() + " " + (pageNum + 1)));
                navigationComponent.addExtra(nextComponent);
            }
            
            player.spigot().sendMessage(navigationComponent);
            player.sendMessage("§7Use: /chatlogs " + type + " " + target.getName() + " <page>");
        }
    }
}