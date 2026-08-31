package com.boes.sage.features.kit.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.kit.KitService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.ArrayList;
import java.util.List;

public class GiveKitCommand {
    private final KitService kitService;

    public GiveKitCommand(Sage plugin) {
        this.kitService = plugin.getKitService();
    }

    @Command("givekit <target> <kit>")
    @Permission("sage.kit.give")
    public void onCommand(
            CommandSender sender,
            @Argument(value = "target", suggestions = "kitGiveTargets") String targetName,
            @Argument(value = "kit", suggestions = "kits") String kitName
    ) {
        if (!kitService.kitExists(kitName)) {
            sender.sendMessage("§cKit '" + kitName + "' does not exist!");
            return;
        }

        if (targetName.equalsIgnoreCase("all")) {
            List<Player> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (recipients.isEmpty()) {
                sender.sendMessage("§cNo players are online.");
                return;
            }

            for (Player recipient : recipients) {
                kitService.forceGiveKit(recipient, kitName);
                recipient.sendMessage("§a§lYou have been given kit '" + kitName + "'!");
            }

            sender.sendMessage("§aForce gave kit '" + kitName + "' to §e" + recipients.size() + " §aplayer(s).");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage("§cPlayer '" + targetName + "' is not online.");
            return;
        }

        kitService.forceGiveKit(target, kitName);
        target.sendMessage("§a§lYou have been given kit '" + kitName + "'!");

        if (sender.equals(target)) {
            sender.sendMessage("§aForce gave yourself kit '" + kitName + "'.");
            return;
        }

        sender.sendMessage("§aForce gave kit '" + kitName + "' to §e" + target.getName() + "§a.");
    }
}
