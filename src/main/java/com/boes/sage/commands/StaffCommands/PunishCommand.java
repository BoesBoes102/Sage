package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import com.boes.sage.data.PunishmentData;
import com.boes.sage.managers.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("punish|p")
@CommandPermission("sage.staff")
public class PunishCommand extends BaseCommand {

    private final Sage plugin;

    public PunishCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players @punishReasons")
    public void onCommand(Player player, String targetName, String reason) {
        reason = reason.toLowerCase();
        
        PunishmentData punishmentData = plugin.getPunishmentReasons().get(reason);

        if (punishmentData == null) {
            player.sendMessage("§cInvalid punishment reason! Available reasons:");
            for (String key : plugin.getPunishmentReasons().keySet()) {
                player.sendMessage("§7- §f" + key);
            }
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined the server!");
            return;
        }

        PunishmentManager pm = plugin.getPunishmentManager();
        int currentStack = pm.getPlayerStack(target.getUniqueId(), reason);
        int newStack = currentStack + 1;

        PunishmentData.StackPunishment punishment = punishmentData.getPunishment(newStack);

        if (punishment == null) {
            player.sendMessage("§cNo punishment defined for stack " + newStack + " of reason " + reason);
            return;
        }

        pm.incrementStack(target.getUniqueId(), reason);

        String type = punishment.type().toLowerCase();
        String duration = punishment.duration();

        switch (type) {
            case "warn":
                pm.warn(target, punishmentData.getReason(), player);
                player.sendMessage("§aWarned " + target.getName() + " for " + punishmentData.getReason() + " (Stack: " + newStack + ")");
                break;
            case "mute":
                pm.mute(target, punishmentData.getReason(), duration, player);
                player.sendMessage("§aMuted " + target.getName() + " for " + duration + " (Stack: " + newStack + ")");
                break;
            case "ban":
                pm.ban(target, punishmentData.getReason(), duration, player);
                player.sendMessage("§aBanned " + target.getName() + " for " + (duration == null ? "permanent" : duration) + " (Stack: " + newStack + ")");
                break;
            case "blacklist":
                pm.blacklist(target, punishmentData.getReason(), player);
                player.sendMessage("§4Blacklisted " + target.getName() + " (Stack: " + newStack + ")");
                break;
            default:
                player.sendMessage("§cInvalid punishment type: " + type);
                break;
        }
    }
}