package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.Objects;

public class UnblacklistCommand {

    private final Sage plugin;

    public UnblacklistCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("unblacklist <player>")
    @Permission("sage.unblacklist")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "bans") String targetName
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            if (!plugin.getPunishmentService().isAlreadyBlacklisted(target)) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§c" + target.getName() + " is not blacklisted!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(target.getName()));

                if (target.isOnline()) {
                    String ip = Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress()).getAddress().getHostAddress();
                    Bukkit.getBanList(BanList.Type.IP).pardon(ip);
                } else {
                    String storedIP = plugin.getPunishmentService().getBannedPlayerIP(target.getUniqueId());
                    if (storedIP != null) {
                        Bukkit.getBanList(BanList.Type.IP).pardon(storedIP);
                    }
                }

                plugin.getPunishmentService().removeBannedPlayerIP(target.getUniqueId());

                issuer.sendMessage("§aRemoved blacklist from " + target.getName());
                plugin.getNotificationService().sendStaffNotice("§e" + issuer.getName() + " §7removed blacklist from §e" + target.getName());
            });
        });
    }
}
