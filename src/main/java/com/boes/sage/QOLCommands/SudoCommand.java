package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("sudo")
@Description("Force a player to execute a command")
@Syntax("<player> <true|false> <command...>")
public class SudoCommand extends BaseCommand {

    private final Sage plugin;

    public SudoCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players true|false")
    @Conditions("permission:sage.sudo")
    public void onCommand(org.bukkit.command.CommandSender sender, Player target, boolean withOp, String[] args) {
        StringBuilder commandBuilder = new StringBuilder();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) commandBuilder.append(" ");
                commandBuilder.append(args[i]);
            }
        }
        String commandToExecute = commandBuilder.toString();

        if (commandToExecute.startsWith("/")) {
            commandToExecute = commandToExecute.substring(1);
        }

        if (commandToExecute.isEmpty()) {
            sender.sendMessage("§cUsage: /sudo <player> <true|false> <command>");
            return;
        }

        boolean wasOp = target.isOp();

        try {
            if (withOp && !wasOp) {
                target.setOp(true);
            }

            target.performCommand(commandToExecute);
            sender.sendMessage("§aForced " + target.getName() + " to execute: /" + commandToExecute);

        } finally {
            if (withOp && !wasOp) {
                target.setOp(false);
            }
        }
    }
}