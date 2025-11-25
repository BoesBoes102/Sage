package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import com.boes.sage.managers.VanishManager;
import org.bukkit.entity.Player;

@CommandAlias("vanish")
@Description("Toggle vanish mode")
@CommandPermission("sage.vanish")
public class VanishCommand extends BaseCommand {

    private final Sage plugin;

    public VanishCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(Player player, @Optional Player target) {
        if (target == null) {
            target = player;
        } else if (!target.equals(player) && !player.hasPermission("sage.staff.admin")) {
            player.sendMessage("§cYou don't have permission to vanish other players!");
            return;
        }

        VanishManager vanishManager = plugin.getVanishManager();

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