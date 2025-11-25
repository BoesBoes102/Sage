package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("c|clear")
@Description("Clear your inventory or another player's inventory")
@CommandPermission("sage.clear")
public class ClearCommand extends BaseCommand {

    private final Sage plugin;

    public ClearCommand(Sage plugin) {
        this.plugin = plugin;
    }
    @Default
    @CommandCompletion("@players")
    public void onCommand(Player sender, @Optional String targetName) {
        Player target = sender;
        
        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        }

        target.getInventory().clear();

        target.getInventory().setHelmet(null);
        target.getInventory().setChestplate(null);
        target.getInventory().setLeggings(null);
        target.getInventory().setBoots(null);

        target.getInventory().setItemInOffHand(new ItemStack(Material.AIR));

        if (sender.equals(target)) {
            target.sendMessage("§aYour inventory has been cleared!");
        } else {
            target.sendMessage("§cYour inventory has been cleared!");
            sender.sendMessage("§aCleared " + target.getName() + "'s inventory!");
        }
    }
}