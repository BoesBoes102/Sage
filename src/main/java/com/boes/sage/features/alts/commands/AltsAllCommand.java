package com.boes.sage.features.alts.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.alts.AltAccountService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;
import java.util.Map;

public class AltsAllCommand {
    private final Sage plugin;

    public AltsAllCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("altsall <player>")
    @Permission("sage.altsall")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = AltsCommand.resolveTarget(playerName);
            if (target == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(ChatColor.RED + "Player " + playerName + " has never joined the server!"));
                return;
            }

            AltAccountService service = plugin.getAltAccountService();
            AltAccountService.AltPlayerRecord record = service.getPlayerRecord(target.getUniqueId());
            if (record == null || record.ips().isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(ChatColor.RED + "No IP history is stored for " + AltsCommand.formatName(target, playerName) + "."));
                return;
            }

            Map<String, List<AltAccountService.AltMatch>> matchesByIp = service.findMatchingAllIps(target.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(ChatColor.GOLD + "Full alt check for " + record.name());
                sender.sendMessage(ChatColor.YELLOW + "Known IPs: " + ChatColor.WHITE + String.join(", ", record.ips()));

                if (matchesByIp.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "No other accounts found on any stored IP.");
                    return;
                }

                for (Map.Entry<String, List<AltAccountService.AltMatch>> entry : matchesByIp.entrySet()) {
                    sender.sendMessage(ChatColor.YELLOW + entry.getKey() + ChatColor.GRAY + ":");
                    for (AltAccountService.AltMatch match : entry.getValue()) {
                        sender.sendMessage(AltsCommand.formatMatchLine(match));
                    }
                }
            });
        });
    }
}
