package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("xp")
@Description("Manage player experience")
public class XpCommand extends BaseCommand {

    private final Sage plugin;

    public XpCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("show")
    @CommandCompletion("@players")
    @Conditions("permission:sage.xp")
    public void onShow(CommandSender sender, @Optional Player target) {
        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        showXp(sender, target);
    }

    @Subcommand("reset")
    @CommandCompletion("@players")
    @Conditions("permission:sage.xp")
    public void onReset(CommandSender sender, @Optional Player target) {
        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        resetXp(sender, target);
    }

    @Subcommand("set")
    @CommandCompletion("@players 1|10|50|100")
    @Conditions("permission:sage.xp")
    public void onSet(CommandSender sender, Player target, int amount) {
        if (amount < 0) {
            sender.sendMessage("§cXP level must be at least 0!");
            return;
        }

        target.setLevel(amount);
        target.setExp(0);
        sender.sendMessage("§aSet " + target.getName() + "'s XP level to " + amount + "!");
        target.sendMessage("§aYour XP level has been set to " + amount + "!");
    }

    @Subcommand("give")
    @CommandCompletion("@players 1|10|50|100")
    @Conditions("permission:sage.xp")
    public void onGive(CommandSender sender, Player target, int amount) {
        if (amount <= 0) {
            sender.sendMessage("§cXP levels must be at least 1!");
            return;
        }

        target.giveExpLevels(amount);
        sender.sendMessage("§aGave " + target.getName() + " " + amount + " XP levels!");
        target.sendMessage("§aYou received " + amount + " XP levels!");
    }

    private void showXp(CommandSender sender, Player target) {
        int level = target.getLevel();
        float progress = target.getExp();
        int progressPercent = (int) (progress * 100);

        sender.sendMessage("§e" + target.getName() + "'s XP:");
        sender.sendMessage("§7Level: §a" + level);
        sender.sendMessage("§7Progress to next level: §a" + progressPercent + "%");
    }

    private void resetXp(CommandSender sender, Player target) {
        target.setLevel(0);
        target.setExp(0);
        if (sender.equals(target)) {
            sender.sendMessage("§aYour XP has been reset to 0!");
        } else {
            sender.sendMessage("§aReset XP for " + target.getName() + "!");
            target.sendMessage("§aYour XP has been reset to 0 by " + sender.getName() + "!");
        }
    }
}