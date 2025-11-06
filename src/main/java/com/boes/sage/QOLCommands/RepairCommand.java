package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

@CommandAlias("repair")
@Description("Repair your items")
public class RepairCommand extends BaseCommand {

    private final Sage plugin;

    public RepairCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("hand|all")
    @Conditions("permission:sage.repair")
    public void onCommand(Player player, String mode) {
        mode = mode.toLowerCase();

        if (mode.equals("hand")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (item.getType().isAir()) {
                player.sendMessage("§cYou must be holding an item!");
                return;
            }

            if (repairItem(item)) {
                player.sendMessage("§aRepaired the item in your hand!");
            } else {
                player.sendMessage("§cThis item cannot be repaired!");
            }
        } else if (mode.equals("all")) {
            int repairedCount = 0;

            for (ItemStack item : player.getInventory().getContents()) {
                if (repairItem(item)) {
                    repairedCount++;
                }
            }

            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (repairItem(item)) {
                    repairedCount++;
                }
            }

            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (repairItem(offHand)) {
                repairedCount++;
            }

            if (repairedCount > 0) {
                player.sendMessage("§aRepaired §e" + repairedCount + " §aitem(s)!");
            } else {
                player.sendMessage("§cNo items to repair!");
            }
        } else {
            player.sendMessage("§cUsage: /repair <hand|all>");
        }
    }

    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            if (damageable.hasDamage()) {
                damageable.setDamage(0);
                item.setItemMeta(meta);
                return true;
            }
        }

        return false;
    }
}