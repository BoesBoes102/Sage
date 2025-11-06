package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;

@CommandAlias("unblacklist")
@CommandPermission("sage.unblacklist")
public class UnblacklistCommand extends BaseCommand {

    private final Sage plugin;

    public UnblacklistCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@bans")
    public void onCommand(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        if (!plugin.getPunishmentManager().isAlreadyBlacklisted(target)) {
            player.sendMessage("§c" + target.getName() + " is not blacklisted!");
            return;
        }

        Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(target.getName()));

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress()).getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        } else {
            String storedIP = plugin.getPunishmentManager().getBannedPlayerIP(target.getUniqueId());
            if (storedIP != null) {
                Bukkit.getBanList(BanList.Type.IP).pardon(storedIP);
            }
        }

        plugin.getPunishmentManager().removeBannedPlayerIP(target.getUniqueId());

        player.sendMessage("§aRemoved blacklist from " + target.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("sage.staff")) {
                p.sendMessage("§e" + player.getName() + " §7removed blacklist from §e" + target.getName());
            }
        }
    }
}