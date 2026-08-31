package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class WarnCommand {

    private final Sage plugin;

    public WarnCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("warn <player> [reason]")
    @Permission("sage.warn")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument("reason") String reason
    ) {
        String finalReason = reason == null ? "No reason specified" : reason;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getPunishmentService().warn(target, finalReason, issuer);
                issuer.sendMessage("§aWarned §e" + target.getName() + " §afor: §f" + finalReason);
            });
        });
    }
}
