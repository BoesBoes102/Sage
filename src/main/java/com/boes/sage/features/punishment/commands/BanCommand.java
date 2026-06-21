package com.boes.sage.features.punishment.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("ban")
@CommandPermission("sage.ban")
public class BanCommand extends BaseCommand {

    private final Sage plugin;

    public BanCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<player> [duration] <reason>")
    @CommandCompletion("@players @durations")
    public void onCommand(Player player, String targetName, @Optional String duration, String[] reason) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        String reasonText = String.join(" ", reason);
        
        plugin.getPunishmentService().ban(target, reasonText, duration, player);
        player.sendMessage("§aBanned " + target.getName() + (duration == null ? " permanently" : " for " + duration));
    }
}
