package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class KickCommand {

    private final Sage plugin;

    public KickCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("kick <player> <reason>")
    @Permission("sage.kick")
    public void onCommand(
            Player issuer,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument("reason") String reason
    ) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            issuer.sendMessage("§cPlayer is not online!");
            return;
        }

        plugin.getPunishmentService().kick(target, reason, issuer);
        issuer.sendMessage("§aKicked " + target.getName() + " for: " + reason);
    }
}
