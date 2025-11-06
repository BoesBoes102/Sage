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

@CommandAlias("unban")
@CommandPermission("sage.unban")
public class UnbanCommand extends BaseCommand {

    private final Sage plugin;

    public UnbanCommand(Sage plugin) {
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

        if (!plugin.getPunishmentManager().isAlreadyBanned(target)) {
            player.sendMessage("§c" + target.getName() + " is not banned!");
            return;
        }

        Bukkit.getBanList(BanList.Type.NAME).pardon(target.getName());

        if (target.isOnline()) {
            String ip = target.getPlayer().getAddress().getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).pardon(ip);
        } else {
            String storedIP = plugin.getPunishmentManager().getBannedPlayerIP(target.getUniqueId());
            if (storedIP != null) {
                Bukkit.getBanList(BanList.Type.IP).pardon(storedIP);
            }
        }

        plugin.getPunishmentManager().removeBannedPlayerIP(target.getUniqueId());

        player.sendMessage("§aUnbanned " + target.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("sage.staff")) {
                p.sendMessage("§e" + player.getName() + " §7unbanned §e" + target.getName());
            }
        }
    }
}