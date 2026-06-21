package com.boes.sage.features.punishment.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("unban")
@CommandPermission("sage.unban")
public class UnbanCommand extends BaseCommand {

    private final Sage plugin;

    public UnbanCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<player>")
    @CommandCompletion("@bans")
    public void onCommand(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("\u00A7cPlayer has never joined!");
            return;
        }

        if (!plugin.getPunishmentService().isAlreadyBanned(target)) {
            player.sendMessage("\u00A7c" + target.getName() + " is not banned!");
            return;
        }

        Bukkit.getBanList(BanList.Type.NAME).pardon(target.getName());

        if (target.isOnline()) {
            String ip = target.getPlayer().getAddress().getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        } else {
            String storedIP = plugin.getPunishmentService().getBannedPlayerIP(target.getUniqueId());
            if (storedIP != null) {
                Bukkit.getBanList(BanList.Type.IP).pardon(storedIP);
            }
        }

        plugin.getPunishmentService().removeBannedPlayerIP(target.getUniqueId());

        player.sendMessage("\u00A7aUnbanned " + target.getName());
        plugin.getNotificationService().sendStaffNotice("\u00A7e" + player.getName() + " \u00A77unbanned \u00A7e" + target.getName());
    }
}
