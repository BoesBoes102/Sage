package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class RepairCommand {

    private final Sage plugin;

    public RepairCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("repair <mode>")
    @Permission("sage.repair")
    public void onCommand(Player player, @Argument(value = "mode", suggestions = "repairModes") String mode) {
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
