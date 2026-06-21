package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import com.boes.sage.features.alts.AltAccountService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("alts")
@Description("Check other accounts linked to a player's latest IP")
@CommandPermission("sage.alts")
public class AltsCommand extends BaseCommand {
    private final Sage plugin;

    public AltsCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(CommandSender sender, String playerName) {
        OfflinePlayer target = resolveTarget(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " has never joined the server!");
            return;
        }

        AltAccountService service = plugin.getAltAccountService();
        AltAccountService.AltPlayerRecord record = service.getPlayerRecord(target.getUniqueId());
        if (record == null || record.lastIp() == null || record.lastIp().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No IP history is stored for " + formatName(target, playerName) + ".");
            return;
        }

        List<AltAccountService.AltMatch> matches = service.findMatchingLatestIp(target.getUniqueId());
        sender.sendMessage(ChatColor.GOLD + "Alt check for " + record.name());
        sender.sendMessage(ChatColor.YELLOW + "Latest IP: " + ChatColor.WHITE + record.lastIp());

        if (matches.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No other accounts found on that IP.");
            return;
        }

        for (AltAccountService.AltMatch match : matches) {
            sender.sendMessage(formatMatchLine(match));
        }
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
