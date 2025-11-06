package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

@CommandAlias("sit")
@Description("Sit down on the ground")
@Conditions("permission:sage.sit")
public class SitCommand extends BaseCommand {

    private final Sage plugin;

    public SitCommand(Sage plugin) {
        this.plugin = plugin;
    }

    public void onCommand(Player player) {
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
            player.sendMessage("§aYou stood up!");
            return;
        }

        ArmorStand armorStand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setSmall(true);
        armorStand.addPassenger(player);

        player.sendMessage("§aYou sat down!");
    }
}