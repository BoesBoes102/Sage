package com.boes.sage.features.punishment.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
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
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("\u00A7cPlayer has never joined!");
            return;
        }

        if (!plugin.getPunishmentService().isMuted(target.getUniqueId())) {
            player.sendMessage("\u00A7c" + target.getName() + " is not muted!");
            return;
        }

        plugin.getPunishmentService().unmute(target.getUniqueId());

        if (target.isOnline()) {
            String ip = target.getPlayer().getAddress().getAddress().getHostAddress();
            plugin.getPunishmentService().unmuteByIP(ip);
        }

        player.sendMessage("\u00A7aUnmuted " + target.getName());
        plugin.getNotificationService().sendStaffNotice("\u00A7e" + player.getName() + " \u00A77unmuted \u00A7e" + target.getName());
    }
}
