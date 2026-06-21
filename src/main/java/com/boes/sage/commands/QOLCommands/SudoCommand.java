package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sudo")
@Description("Force a player to execute a command")
@CommandPermission("sage.sudo")
public class SudoCommand extends BaseCommand {

    public SudoCommand(Sage plugin) {
    }

    @Default
    @CommandCompletion("@players true|false|chat ")
    @Syntax("<player> <true|false|chat> <command/message...>")
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /sudo <player> <true|false|chat> <command/message>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "You must specify a valid player.");
            return;
        }

        String mode = args[1].toLowerCase();
        if (!mode.equals("true") && !mode.equals("false") && !mode.equals("chat")) {
            sender.sendMessage(ChatColor.RED + "Second argument must be true, false, or chat.");
            return;
        }

        String[] inputArgs = new String[args.length - 2];
        System.arraycopy(args, 2, inputArgs, 0, args.length - 2);
        String input = String.join(" ", inputArgs);

        if (mode.equals("chat")) {
            target.chat(input);
            sender.sendMessage(ChatColor.GREEN + "Forced " + target.getName() + " to chat: " + ChatColor.WHITE + input);
            return;
        }

        boolean withOp = mode.equals("true");
        String commandToExecute = input.startsWith("/") ? input.substring(1) : input;
        boolean elevatePermissions = withOp && !target.isOp();
        boolean wasOp = target.isOp();

        try {
            if (elevatePermissions) {
                target.setOp(true);
            }

            Bukkit.dispatchCommand(target, commandToExecute);
            sender.sendMessage(ChatColor.GREEN + "Forced " + target.getName() + " to execute: " + ChatColor.WHITE + "/" + commandToExecute);
        } finally {
            if (elevatePermissions) {
                target.setOp(wasOp);
            }
        }
    }
}
