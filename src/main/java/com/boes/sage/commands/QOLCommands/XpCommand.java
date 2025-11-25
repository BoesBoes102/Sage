package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("xp")
@Description("Manage player experience")
@CommandPermission("sage.xp")
public class XpCommand extends BaseCommand {

    private final Sage plugin;

    public XpCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onDefault(CommandSender sender) {
        sender.sendMessage("§eXP Commands:");
        sender.sendMessage("§6/xp show [player] §7- Show XP");
        sender.sendMessage("§6/xp reset [player] §7- Reset XP");
        sender.sendMessage("§6/xp set <player> <amount> §7- Set XP");
        sender.sendMessage("§6/xp give <player> <amount> §7- Give XP");
    }
    @Subcommand("show")
    @CommandCompletion("@players")
    public void onShow(CommandSender sender, String[] args) {
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

        showXp(sender, target);
    }

    @Subcommand("reset")
    @CommandCompletion("@players")
    public void onReset(CommandSender sender, String[] args) {
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

        resetXp(sender, target);
    }

    @Subcommand("set")
    @CommandCompletion("@players 1|10|50|100")
    public void onSet(CommandSender sender, String[] args) {
        Player target = null;
        int amount = 0;

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /xp set [player] <amount>");
            return;
        }

        try {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cYou must specify a player from console!");
                    return;
                }
                target = (Player) sender;
                amount = Integer.parseInt(args[0]);
            } else {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }
                amount = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number!");
            return;
        }

        if (amount < 0) {
            sender.sendMessage("§cXP level must be at least 0!");
            return;
        }

        target.setLevel(amount);
        target.setExp(0);
        
        if (sender.equals(target)) {
            sender.sendMessage("§aYour XP level has been set to " + amount + "!");
        } else {
            sender.sendMessage("§aSet " + target.getName() + "'s XP level to " + amount + "!");
            target.sendMessage("§aYour XP level has been set to " + amount + "!");
        }
    }

    @Subcommand("give")
    @CommandCompletion("@players 1|10|50|100")
    public void onGive(CommandSender sender, String[] args) {
        Player target = null;
        int amount = 0;

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /xp give [player] <amount>");
            return;
        }

        try {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cYou must specify a player from console!");
                    return;
                }
                target = (Player) sender;
                amount = Integer.parseInt(args[0]);
            } else {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }
                amount = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number!");
            return;
        }

        if (amount <= 0) {
            sender.sendMessage("§cXP levels must be at least 1!");
            return;
        }

        target.giveExpLevels(amount);
        
        if (sender.equals(target)) {
            sender.sendMessage("§aYou received " + amount + " XP levels!");
        } else {
            sender.sendMessage("§aGave " + target.getName() + " " + amount + " XP levels!");
            target.sendMessage("§aYou received " + amount + " XP levels!");
        }
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
            target.sendMessage("§aYour XP has been reset to 0!");
        }
    }
}