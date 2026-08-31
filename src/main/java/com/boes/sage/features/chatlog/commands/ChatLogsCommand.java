package com.boes.sage.features.chatlog.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.chatlog.data.ChatLogEntry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatLogsCommand {
    private final Sage plugin;

    public ChatLogsCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("chatlogs <type> <player> [page]")
    @Permission("sage.chatlogs")
    public void onCommand(
            Player issuer,
            @Argument(value = "type", suggestions = "chatLogTypes") String type,
            @Argument(value = "player", suggestions = "players") String playerName,
            @Argument(value = "page", suggestions = "chatLogPages") Integer page
    ) {
        if (!type.equalsIgnoreCase("message") && !type.equalsIgnoreCase("command")) {
            issuer.sendMessage("§cType must be 'message' or 'command'!");
            return;
        }

        int pageNum = page != null ? page : 1;
        if (pageNum < 1) {
            issuer.sendMessage("§cPage number must be at least 1!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (!target.hasPlayedBefore()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            List<ChatLogEntry> logs = plugin.getChatLogService().getLogsPage(target.getUniqueId(), type, pageNum);
            int totalPages = plugin.getChatLogService().getTotalPages(target.getUniqueId(), type);

            if (logs.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        issuer.sendMessage("§cNo " + type + " logs found for " + target.getName() + "!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                issuer.sendMessage("§e========== " + target.getName() + "'s " + type.toUpperCase() + " Logs ==========");
                issuer.sendMessage("§7Page " + pageNum + " of " + totalPages);
                issuer.sendMessage("");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                for (ChatLogEntry log : logs) {
                    String date = sdf.format(new Date(log.getTimestamp()));
                    issuer.sendMessage("§7[§f" + date + "§7] §f" + log.getMessage());
                }

                issuer.sendMessage("");

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

                    issuer.spigot().sendMessage(navigationComponent);
                    issuer.sendMessage("§7Use: /chatlogs " + type + " " + target.getName() + " <page>");
                }
            });
        });
    }
}
