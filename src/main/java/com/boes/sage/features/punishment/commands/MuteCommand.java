package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class MuteCommand {

    private final Sage plugin;

    public MuteCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("mute <player> <duration> [reason]")
    @Permission("sage.mute")
    public void onCommand(
            CommandSender issuer,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument(value = "duration", suggestions = "durations") String duration,
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
                plugin.getPunishmentService().mute(target, finalReason, duration, issuer);
                issuer.sendMessage("§aMuted " + target.getName() + " for " + duration);
            });
        });
    }
}
