package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class UnmuteCommand {

    private final Sage plugin;

    public UnmuteCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("unmute <player>")
    @Permission("sage.unmute")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "players") String targetName
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            if (!plugin.getPunishmentService().isMuted(target.getUniqueId())) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§c" + target.getName() + " is not muted!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getPunishmentService().unmute(target.getUniqueId());

                if (target.isOnline()) {
                    String ip = target.getPlayer().getAddress().getAddress().getHostAddress();
                    plugin.getPunishmentService().unmuteByIP(ip);
                }

                issuer.sendMessage("§aUnmuted " + target.getName());
                plugin.getNotificationService().sendStaffNotice("§e" + issuer.getName() + " §7unmuted §e" + target.getName());
            });
        });
    }
}
