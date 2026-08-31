package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class SpeedCommand {

    private final Sage plugin;

    public SpeedCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("speed <number> [mode] [player]")
    @Permission("sage.speed")
    public void onCommand(
            CommandSender sender,
            @Argument(value = "number", suggestions = "speedOptions") String numberArg,
            @Argument(value = "mode", suggestions = "speedModes") String mode,
            @Argument(value = "player", suggestions = "players") String targetName
    ) {
        if (numberArg.equalsIgnoreCase("reset")) {
            applyReset(sender, targetName);
            return;
        }

        mode = mode != null ? mode.toLowerCase() : "both";
        if (!mode.equals("fly") && !mode.equals("walk") && !mode.equals("both")) {
            sender.sendMessage("§cUsage: /speed <1-10|reset> [fly|walk|both] [player]");
            return;
        }

        applySpeed(sender, numberArg, mode, targetName);
    }

    private void applyReset(CommandSender sender, String targetName) {
        Player target;

        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
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

    private void applySpeed(CommandSender sender, String numberArg, String mode, String targetName) {
        float speed;
        try {
            speed = Float.parseFloat(numberArg);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cSpeed must be a number!");
            return;
        }

        if (speed < 0 || speed > 10) {
            sender.sendMessage("§cSpeed must be between 0 and 10!");
            return;
        }

        Player target = targetName != null ? Bukkit.getPlayer(targetName) : null;

        if (target == null) {
            if (targetName != null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
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
                sender.sendMessage("§cUsage: /speed <1-10|reset> [fly|walk|both] [player]");
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
