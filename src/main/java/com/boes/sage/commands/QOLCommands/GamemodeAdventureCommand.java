package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("adventure|gma|gm4")
@Description("Set gamemode to Adventure")
@CommandPermission("sage.gamemode.adventure")
public class GamemodeAdventureCommand extends BaseCommand {

    private final Sage plugin;

    public GamemodeAdventureCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @CommandCompletion("@players")
    public void onCommand(CommandSender sender, @Optional String targetName) {
        Player target = null;
        
        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
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
            target.sendMessage("§aYour gamemode has been set to Adventure!");
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to Adventure!");
        }
    }
}