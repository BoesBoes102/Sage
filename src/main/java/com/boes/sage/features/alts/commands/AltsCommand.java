package com.boes.sage.features.alts.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.alts.AltAccountService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

public class AltsCommand {
    private final Sage plugin;

    public AltsCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("alts <player>")
    @Permission("sage.alts")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = resolveTarget(playerName);
            if (target == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(ChatColor.RED + "Player " + playerName + " has never joined the server!"));
                return;
            }

            AltAccountService service = plugin.getAltAccountService();
            AltAccountService.AltPlayerRecord record = service.getPlayerRecord(target.getUniqueId());
            if (record == null || record.lastIp() == null || record.lastIp().isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(ChatColor.RED + "No IP history is stored for " + formatName(target, playerName) + "."));
                return;
            }

            List<AltAccountService.AltMatch> matches = service.findMatchingLatestIp(target.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                sender.sendMessage(ChatColor.GOLD + "Alt check for " + record.name());
                sender.sendMessage(ChatColor.YELLOW + "Latest IP: " + ChatColor.WHITE + record.lastIp());

                if (matches.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "No other accounts found on that IP.");
                    return;
                }

                for (AltAccountService.AltMatch match : matches) {
                    sender.sendMessage(formatMatchLine(match));
                }
            });
        });
    }

    static OfflinePlayer resolveTarget(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (!offlinePlayer.hasPlayedBefore()) {
            return null;
        }
        return offlinePlayer;
    }

    static String formatName(OfflinePlayer player, String fallback) {
        return player.getName() != null ? player.getName() : fallback;
    }

    static String formatMatchLine(AltAccountService.AltMatch match) {
        String status = match.online() ? ChatColor.GREEN + "online" : ChatColor.GRAY + "offline";
        return ChatColor.YELLOW + "- " + ChatColor.WHITE + match.name() + ChatColor.DARK_GRAY + " (" + status + ChatColor.DARK_GRAY + ")";
    }
}
