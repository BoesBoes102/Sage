package com.boes.sage.features.kit.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.features.kit.KitService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("givekit")
@CommandPermission("sage.kit.give")
public class GiveKitCommand extends BaseCommand {
    private final KitService kitService;

    public GiveKitCommand(Sage plugin) {
        this.kitService = plugin.getKitService();
    }

    @Default
    @Syntax("<player|all> <kit>")
    @CommandCompletion("@kitGiveTargets @kits")
    public void onCommand(CommandSender sender, String targetName, String kitName) {
        if (!kitService.kitExists(kitName)) {
            sender.sendMessage("Â§cKit '" + kitName + "' does not exist!");
            return;
        }

        if (targetName.equalsIgnoreCase("all")) {
            List<Player> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (recipients.isEmpty()) {
                sender.sendMessage("Â§cNo players are online.");
                return;
            }

            for (Player recipient : recipients) {
                kitService.forceGiveKit(recipient, kitName);
                recipient.sendMessage("Â§aÂ§lYou have been given kit '" + kitName + "'!");
            }

            sender.sendMessage("Â§aForce gave kit '" + kitName + "' to Â§e" + recipients.size() + " Â§aplayer(s).");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage("Â§cPlayer '" + targetName + "' is not online.");
            return;
        }

        kitService.forceGiveKit(target, kitName);
        target.sendMessage("Â§aÂ§lYou have been given kit '" + kitName + "'!");

        if (sender.equals(target)) {
            sender.sendMessage("Â§aForce gave yourself kit '" + kitName + "'.");
            return;
        }

        sender.sendMessage("Â§aForce gave kit '" + kitName + "' to Â§e" + target.getName() + "Â§a.");
    }
}
