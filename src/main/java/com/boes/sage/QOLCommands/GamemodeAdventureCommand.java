package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.boes.sage.Sage;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("adventure")
@Description("Set gamemode to Adventure")
public class GamemodeAdventureCommand extends BaseCommand {

    private final Sage plugin;

    public GamemodeAdventureCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.gamemode.adventure")
    public void onCommand(CommandSender sender, @Optional Player target) {
        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        target.setGameMode(GameMode.ADVENTURE);

        if (sender.equals(target)) {
            target.sendMessage("§aYour gamemode has been set to Adventure!");
        } else {
            target.sendMessage("§aYour gamemode has been set to Adventure by " + sender.getName() + "!");
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to Adventure!");
        }
    }
}