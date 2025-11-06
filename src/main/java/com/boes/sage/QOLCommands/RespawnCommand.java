package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.entity.Player;

@CommandAlias("respawn")
@Description("Kill a player, sending them to spawn")
public class RespawnCommand extends BaseCommand {

    private final Sage plugin;

    public RespawnCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.respawn")
    public void onCommand(org.bukkit.command.CommandSender sender, @Optional Player target) {
        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        target.setHealth(0);

        if (sender.equals(target)) {
            sender.sendMessage("§aYou have been killed!");
        } else {
            sender.sendMessage("§aKilled " + target.getName() + "!");
        }
    }
}