package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("unmute")
@CommandPermission("sage.unmute")
public class UnmuteCommand extends BaseCommand {

    private final Sage plugin;

    public UnmuteCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        if (!plugin.getPunishmentManager().isMuted(target.getUniqueId())) {
            player.sendMessage("§c" + target.getName() + " is not muted!");
            return;
        }

        plugin.getPunishmentManager().unmute(target.getUniqueId());

        if (target.isOnline()) {
            String ip = target.getPlayer().getAddress().getAddress().getHostAddress();
            plugin.getPunishmentManager().unmuteByIP(ip);
        }

        player.sendMessage("§aUnmuted " + target.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("sage.staff")) {
                p.sendMessage("§e" + player.getName() + " §7unmuted §e" + target.getName());
            }
        }
    }
}