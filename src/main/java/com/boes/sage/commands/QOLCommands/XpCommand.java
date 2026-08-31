package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class XpCommand {

    private final Sage plugin;

    public XpCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("xp")
    @Permission("sage.xp")
    public void onDefault(CommandSender sender) {
        sender.sendMessage("§eXP Commands:");
        sender.sendMessage("§6/xp show [player] §7- Show XP");
        sender.sendMessage("§6/xp reset [player] §7- Reset XP");
        sender.sendMessage("§6/xp set <player> <amount> §7- Set XP");
        sender.sendMessage("§6/xp give <player> <amount> §7- Give XP");
    }

    @Command("xp show [player]")
    @Permission("sage.xp")
    public void onShow(CommandSender sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target;

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

        showXp(sender, target);
    }

    @Command("xp reset [player]")
    @Permission("sage.xp")
    public void onReset(CommandSender sender, @Argument(value = "player", suggestions = "players") String targetName) {
        Player target;

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

        resetXp(sender, target);
    }

    @Command("xp set <player> <amount>")
    @Permission("sage.xp")
    public void onSet(
            CommandSender sender,
            @Argument(value = "player", suggestions = "players") String player,
            @Argument("amount") String amount
    ) {
        Player target = Bukkit.getPlayer(player);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        applySet(sender, target, amount);
    }

    private void applySet(CommandSender sender, Player target, String amountArg) {
        int amount;
        try {
            amount = Integer.parseInt(amountArg);
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

    @Command("xp give <player> <amount>")
    @Permission("sage.xp")
    public void onGive(
            CommandSender sender,
            @Argument(value = "player", suggestions = "players") String player,
            @Argument("amount") String amount
    ) {
        Player target = Bukkit.getPlayer(player);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        applyGive(sender, target, amount);
    }

    private void applyGive(CommandSender sender, Player target, String amountArg) {
        int amount;
        try {
            amount = Integer.parseInt(amountArg);
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
