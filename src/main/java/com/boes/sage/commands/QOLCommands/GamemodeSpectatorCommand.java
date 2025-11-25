package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("spectator|gmsp|gm3")
@Description("Set gamemode to Spectator")
@CommandPermission("sage.gamemode.spectator")
public class GamemodeSpectatorCommand extends BaseCommand {

    private final Sage plugin;

    public GamemodeSpectatorCommand(Sage plugin) {
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

        target.setGameMode(GameMode.SPECTATOR);

        if (sender.equals(target)) {
            target.sendMessage("§aYour gamemode has been set to Spectator!");
        } else {
            target.sendMessage("§aYour gamemode has been set to Specatator!");
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to Spectator!");
        }
    }
}