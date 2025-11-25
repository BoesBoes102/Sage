package com.boes.sage.commands.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("mute")
@CommandPermission("sage.mute")
public class MuteCommand extends BaseCommand {

    private final Sage plugin;

    public MuteCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players @durations")
    public void onCommand(Player player, String targetName, String duration, String[] reason) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cPlayer has never joined!");
            return;
        }

        String reasonText = String.join(" ", reason);
        plugin.getPunishmentManager().mute(target, reasonText, duration, player);
        player.sendMessage("§aMuted " + target.getName() + " for " + duration);
    }
}