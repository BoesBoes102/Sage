package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("feed")
@Description("Feed yourself or another player")
@CommandPermission("sage.feed")
public class FeedCommand extends BaseCommand {

    private final Sage plugin;

    public FeedCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onCommand(Player sender, String[] args) {
        Player target = null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
            target = sender;
        }

        target.setFoodLevel(20);
        target.setSaturation(20.0f);

        if (sender.equals(target)) {
            target.sendMessage("§aYou have been fed!");
        } else {
            target.sendMessage("§aYou have been fed by " + sender.getName() + "!");
            sender.sendMessage("§aFed " + target.getName() + "!");
        }
    }
}
