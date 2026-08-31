package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class BanCommand {

    private final Sage plugin;

    public BanCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("ban <player> <duration> [reason]")
    @Permission("sage.ban")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument(value = "duration", suggestions = "durations") String duration,
            @Argument("reason") String reason
    ) {
        String finalReason = reason == null ? "No reason specified" : reason;
        String resolvedDuration = duration.equalsIgnoreCase("permanent") ? null : duration;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined!"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getPunishmentService().ban(target, finalReason, resolvedDuration, issuer);
                issuer.sendMessage("§aBanned " + target.getName() + (resolvedDuration == null ? " permanently" : " for " + resolvedDuration));
            });
        });
    }
}
