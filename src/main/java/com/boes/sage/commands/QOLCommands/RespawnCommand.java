package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("respawn|kill|die")
@Description("Kill a player, sending them to spawn")
@CommandPermission("sage.respawn")
public class RespawnCommand extends BaseCommand {

    private final Sage plugin;

    public RespawnCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @CommandCompletion("@players")
    public void onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        Player target = null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
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

        target.setHealth(0);

        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage("§aYou have been killed!");
        } else {
            sender.sendMessage("§aKilled " + target.getName() + "!");
        }
    }
}