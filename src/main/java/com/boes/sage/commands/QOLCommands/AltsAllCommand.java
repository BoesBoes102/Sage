package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import com.boes.sage.features.alts.AltAccountService;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

@CommandAlias("altsall")
@Description("Check other accounts linked to any IP a player has used")
@CommandPermission("sage.altsall")
public class AltsAllCommand extends BaseCommand {
    private final Sage plugin;

    public AltsAllCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(CommandSender sender, String playerName) {
        OfflinePlayer target = AltsCommand.resolveTarget(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " has never joined the server!");
            return;
        }

        AltAccountService service = plugin.getAltAccountService();
        AltAccountService.AltPlayerRecord record = service.getPlayerRecord(target.getUniqueId());
        if (record == null || record.ips().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No IP history is stored for " + AltsCommand.formatName(target, playerName) + ".");
            return;
        }

        Map<String, List<AltAccountService.AltMatch>> matchesByIp = service.findMatchingAllIps(target.getUniqueId());
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
    }
}
