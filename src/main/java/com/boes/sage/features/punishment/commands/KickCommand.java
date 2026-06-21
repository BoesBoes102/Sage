package com.boes.sage.features.punishment.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("kick")
@CommandPermission("sage.kick")
public class KickCommand extends BaseCommand {

    private final Sage plugin;

    public KickCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<player> <reason>")
    @CommandCompletion("@players")
    public void onCommand(Player player, String targetName, String[] reason) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cPlayer is not online!");
            return;
        }

        String reasonText = String.join(" ", reason);
        plugin.getPunishmentService().kick(target, reasonText, player);
        player.sendMessage("§aKicked " + target.getName() + " for: " + reasonText);
    }
}
