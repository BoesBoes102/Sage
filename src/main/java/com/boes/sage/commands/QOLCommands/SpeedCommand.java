package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("speed")
@Description("Modify player speed")
@CommandPermission("sage.speed")
public class SpeedCommand extends BaseCommand {

    private final Sage plugin;

    public SpeedCommand(Sage plugin) {
        this.plugin = plugin;
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
            if (!target.equals(sender) && !sender.hasPermission("sage.staff.admin")) {
                sender.sendMessage("§cYou don't have permission to reset other players' speed!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        resetSpeed(target);

        if (sender.equals(target)) {
            target.sendMessage("§eSpeed reset to default!");
        } else {
            target.sendMessage("§eSpeed reset to default by " + sender.getName() + "!");
            sender.sendMessage("§eReset speed to default for " + target.getName() + "!");
        }
    }

    @Default
    @CommandCompletion("1|2|3|4|5|6|7|8|9|10 fly|walk|both @players")
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /speed <number> [fly|walk|both] [player]");
            return;
        }

        float speed;
        try {
            speed = Float.parseFloat(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cSpeed must be a number!");
            return;
        }

        if (speed < 0 || speed > 10) {
            sender.sendMessage("§cSpeed must be between 0 and 10!");
            return;
        }

        String mode = "both";
        Player target = null;

        if (args.length > 1 && (args[args.length - 1].equalsIgnoreCase("fly") || args[args.length - 1].equalsIgnoreCase("walk") || args[args.length - 1].equalsIgnoreCase("both"))) {
            mode = args[args.length - 1].toLowerCase();
            if (args.length > 2) {
                target = Bukkit.getPlayer(args[args.length - 2]);
            }
        } else if (args.length > 1) {
            target = Bukkit.getPlayer(args[args.length - 1]);
        }

        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        } else if (!target.equals(sender) && !sender.hasPermission("sage.staff.admin")) {
            sender.sendMessage("§cYou don't have permission to change other players' speed!");
            return;
        }

        float mcSpeed = speed / 10.0f;

        switch (mode) {
            case "fly":
                target.setFlySpeed(mcSpeed);
                break;

            case "walk":
                target.setWalkSpeed(mcSpeed);
                break;

            case "both":
                target.setFlySpeed(mcSpeed);
                target.setWalkSpeed(mcSpeed);
                break;

            default:
                sender.sendMessage("§cUsage: /speed <number> [fly|walk|both] [player]");
                return;
        }

        if (sender.equals(target)) {
            target.sendMessage("§aSpeed set to §e" + speed + "§a (" + mode + ")!");
        } else {
            target.sendMessage("§aYour " + mode + " speed was set to §e" + speed + "§a by " + sender.getName() + "!");
            sender.sendMessage("§aSet " + mode + " speed for " + target.getName() + " to §e" + speed + "§a!");
        }
    }

    private void resetSpeed(Player player) {
        player.setFlySpeed(0.1f);
        player.setWalkSpeed(0.2f);
    }
}