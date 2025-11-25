package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sudo")
@Description("Force a player to execute a command")
@CommandPermission("sage.sudo")
public class SudoCommand extends BaseCommand {

    private final Sage plugin;

    public SudoCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players true|false @commands")
    @Syntax("<player> <true|false> <command...>")
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /sudo <player> <true|false> <command>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cYou must specify a valid player.");
            return;
        }

        boolean withOp;
        try {
            withOp = Boolean.parseBoolean(args[1]);
        } catch (Exception e) {
            sender.sendMessage("§cSecond argument must be true or false!");
            return;
        }

        String[] commandArgs = new String[args.length - 2];
        System.arraycopy(args, 2, commandArgs, 0, args.length - 2);
        String commandToExecute = String.join(" ", commandArgs);

        if (commandToExecute.startsWith("/")) {
            commandToExecute = commandToExecute.substring(1);
        }

        boolean wasOp = target.isOp();

        try {
            if (withOp && !wasOp) {
                target.setOp(true);
            }

            target.performCommand(commandToExecute);
            sender.sendMessage("§aForced " + target.getName() + " to execute: §f/" + commandToExecute);

        } finally {
            if (withOp && !wasOp) {
                target.setOp(false);
            }
        }
    }
}