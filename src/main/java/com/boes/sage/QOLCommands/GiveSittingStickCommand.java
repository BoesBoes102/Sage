package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@CommandAlias("givesittingstick")
@Description("Get a sitting stick for marking seats")
public class GiveSittingStickCommand extends BaseCommand {

    private final Sage plugin;

    public GiveSittingStickCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Conditions("permission:sage.givesittingstick")
    public void onCommand(Player player) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Sitting Stick");
            meta.setCustomModelData(1001);
            stick.setItemMeta(meta);
        }

        player.getInventory().addItem(stick);
        player.sendMessage("§aYou received a Sitting Stick! Right-click stairs, slabs, or carpets to mark them.");
    }
}