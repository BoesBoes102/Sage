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
import org.bukkit.entity.Player;

@CommandAlias("speed")
@Description("Modify player speed")
public class SpeedCommand extends BaseCommand {

    private final Sage plugin;

    public SpeedCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reset")
    @CommandCompletion("@players")
    @Conditions("permission:sage.speed")
    public void onReset(org.bukkit.command.CommandSender sender, @Optional Player target) {
        if (target == null) {
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

    @CommandCompletion("1|2|3|4|5|6|7|8|9|10 fly|walk|both @players")
    @Conditions("permission:sage.speed")
    public void onCommand(org.bukkit.command.CommandSender sender, float speed, String mode, @Optional Player target) {
        if (speed < 0 || speed > 10) {
            sender.sendMessage("§cSpeed must be between 0 and 10!");
            return;
        }

        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player from console!");
                return;
            }
            target = (Player) sender;
        }

        float mcSpeed = speed / 10.0f;

        mode = mode.toLowerCase();

        switch (mode) {
            case "fly":
                target.setFlySpeed(mcSpeed);
                if (sender.equals(target)) {
                    target.sendMessage("§aFly speed set to §e" + speed + "§a!");
                } else {
                    target.sendMessage("§aFly speed set to §e" + speed + " §aby " + sender.getName() + "!");
                    sender.sendMessage("§aSet fly speed to §e" + speed + " §afor " + target.getName() + "!");
                }
                break;
            case "walk":
                target.setWalkSpeed(mcSpeed);
                if (sender.equals(target)) {
                    target.sendMessage("§aWalk speed set to §e" + speed + "§a!");
                } else {
                    target.sendMessage("§aWalk speed set to §e" + speed + " §aby " + sender.getName() + "!");
                    sender.sendMessage("§aSet walk speed to §e" + speed + " §afor " + target.getName() + "!");
                }
                break;
            case "both":
                target.setFlySpeed(mcSpeed);
                target.setWalkSpeed(mcSpeed);
                if (sender.equals(target)) {
                    target.sendMessage("§aFly and walk speed set to §e" + speed + "§a!");
                } else {
                    target.sendMessage("§aFly and walk speed set to §e" + speed + " §aby " + sender.getName() + "!");
                    sender.sendMessage("§aSet fly and walk speed to §e" + speed + " §afor " + target.getName() + "!");
                }
                break;
            default:
                sender.sendMessage("§cUsage: /speed <number|reset> <fly/walk/both> [player]");
        }
    }

    private void resetSpeed(Player player) {
        player.setFlySpeed(0.1f);
        player.setWalkSpeed(0.2f);
    }
}