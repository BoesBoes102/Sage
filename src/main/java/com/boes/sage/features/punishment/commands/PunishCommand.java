package com.boes.sage.features.punishment.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.punishment.data.PunishmentData;
import com.boes.sage.features.punishment.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class PunishCommand {

    private final Sage plugin;

    public PunishCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("punish <player> <reason>")
    @Command("p <player> <reason>")
    @Permission("sage.staff")
    public void onCommand(
            Player issuer,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument(value = "reason", suggestions = "punishReasons") String reasonInput
    ) {
        String reason = reasonInput.toLowerCase();

        PunishmentData punishmentData = plugin.getPunishmentReasons().get(reason);

        if (punishmentData == null) {
            issuer.sendMessage("§cInvalid punishment reason! Available reasons:");
            for (String key : plugin.getPunishmentReasons().keySet()) {
                issuer.sendMessage("§7- §f" + key);
            }
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> issuer.sendMessage("§cPlayer has never joined the server!"));
                return;
            }

            PunishmentService pm = plugin.getPunishmentService();
            int currentStack = pm.getPlayerStack(target.getUniqueId(), reason);
            int newStack = currentStack + 1;

            PunishmentData.StackPunishment punishment = punishmentData.getPunishment(newStack);

            if (punishment == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        issuer.sendMessage("§cNo punishment defined for stack " + newStack + " of reason " + reason));
                return;
            }

            pm.incrementStack(target.getUniqueId(), reason);

            String type = punishment.type().toLowerCase();
            String duration = punishment.duration();

            Bukkit.getScheduler().runTask(plugin, () -> {
                switch (type) {
                    case "warn":
                        pm.warn(target, punishmentData.getReason(), issuer);
                        issuer.sendMessage("§aWarned " + target.getName() + " for " + punishmentData.getReason() + " (Stack: " + newStack + ")");
                        break;
                    case "mute":
                        pm.mute(target, punishmentData.getReason(), duration, issuer);
                        issuer.sendMessage("§aMuted " + target.getName() + " for " + duration + " (Stack: " + newStack + ")");
                        break;
                    case "ban":
                        pm.ban(target, punishmentData.getReason(), duration, issuer);
                        issuer.sendMessage("§aBanned " + target.getName() + " for " + (duration == null ? "permanent" : duration) + " (Stack: " + newStack + ")");
                        break;
                    case "blacklist":
                        pm.blacklist(target, punishmentData.getReason(), issuer);
                        issuer.sendMessage("§4Blacklisted " + target.getName() + " (Stack: " + newStack + ")");
                        break;
                    default:
                        issuer.sendMessage("§cInvalid punishment type: " + type);
                        break;
                }
            });
        });
    }
}
