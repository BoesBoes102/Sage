package com.boes.sage.features.vanish.commands;

import com.boes.sage.Sage;
import com.boes.sage.features.vanish.VanishService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class VanishCommand {

    private final Sage plugin;

    public VanishCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("vanish [target]")
    @Permission("sage.vanish")
    public void onCommand(
            Player player,
            @Argument("target") Player target
    ) {
        if (target == null) {
            target = player;
        } else if (!target.equals(player) && !player.hasPermission("sage.staff.admin")) {
            player.sendMessage("§cYou don't have permission to vanish other players!");
            return;
        }

        VanishService vanishManager = plugin.getVanishService();

        if (vanishManager.isVanished(target)) {
            vanishManager.setVanished(target, false);
            if (target.equals(player)) {
                player.sendMessage("§aYou are no longer vanished!");
            } else {
                player.sendMessage("§a" + target.getName() + " is no longer vanished!");
                target.sendMessage("§aYou are no longer vanished by " + player.getName() + "!");
            }
        } else {
            vanishManager.setVanished(target, true);
            if (target.equals(player)) {
                player.sendMessage("§aYou are now vanished!");
            } else {
                player.sendMessage("§a" + target.getName() + " is now vanished!");
                target.sendMessage("§aYou are now vanished by " + player.getName() + "!");
            }
        }
    }
}
