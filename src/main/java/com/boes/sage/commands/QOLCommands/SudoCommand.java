package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

public class SudoCommand {

    private final Sage plugin;

    public SudoCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("sudo <target> <mode> <command>")
    @Permission("sage.sudo")
    public void onCommand(
            CommandSender sender,
            @Argument(value = "target", suggestions = "sudoTargets") String targetArg,
            @Argument(value = "mode", suggestions = "sudoModes") String mode,
            @Argument(value = "command", suggestions = "none") @Greedy String input
    ) {
        mode = mode.toLowerCase();
        if (!mode.equals("true") && !mode.equals("false") && !mode.equals("chat")) {
            sender.sendMessage(ChatColor.RED + "Second argument must be true, false, or chat.");
            return;
        }

        List<? extends Player> targets;
        if (targetArg.equalsIgnoreCase("@a")) {
            targets = List.copyOf(Bukkit.getOnlinePlayers());
            if (targets.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "No players are online.");
                return;
            }
        } else {
            Player target = Bukkit.getPlayer(targetArg);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "You must specify a valid player.");
                return;
            }
            targets = List.of(target);
        }

        for (Player target : targets) {
            if (mode.equals("chat")) {
                target.chat(input);
                sender.sendMessage(ChatColor.GREEN + "Forced " + target.getName() + " to chat: " + ChatColor.WHITE + input);
                continue;
            }

            boolean withOp = mode.equals("true");
            String commandToExecute = input.startsWith("/") ? input.substring(1) : input;
            PermissionAttachment bypassAttachment = withOp ? target.addAttachment(plugin) : null;
            if (bypassAttachment != null) {
                bypassAttachment.setPermission("*", true);
            }

            try {
                Bukkit.dispatchCommand(target, commandToExecute);
                sender.sendMessage(ChatColor.GREEN + "Forced " + target.getName() + " to execute: " + ChatColor.WHITE + "/" + commandToExecute);
            } finally {
                if (bypassAttachment != null) {
                    target.removeAttachment(bypassAttachment);
                }
            }
        }
    }
}
