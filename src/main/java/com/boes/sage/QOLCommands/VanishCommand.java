package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import com.boes.sage.managers.VanishManager;
import org.bukkit.entity.Player;

@CommandAlias("vanish")
@Description("Toggle vanish mode")
@Conditions("permission:sage.vanish")
public class VanishCommand extends BaseCommand {

    private final Sage plugin;

    public VanishCommand(Sage plugin) {
        this.plugin = plugin;
    }

    public void onCommand(Player player) {
        VanishManager vanishManager = plugin.getVanishManager();

        if (vanishManager.isVanished(player)) {
            vanishManager.setVanished(player, false);
            player.sendMessage("§aYou are no longer vanished!");
        } else {
            vanishManager.setVanished(player, true);
            player.sendMessage("§aYou are now vanished!");
        }
    }
}