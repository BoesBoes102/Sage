package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.PotionMeta;

@CommandAlias("potion")
@Description("Create custom potions")
public class PotionCommand extends BaseCommand {

    private final Sage plugin;

    public PotionCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("normal|splash|lingering @potioneffecttypes 0|1|2|3|4|5 30|60|120|300|600 1|10|64")
    @Conditions("permission:sage.potion")
    public void onCommand(Player player, String type, PotionEffectType potionType, int amplifier, int duration, int amount) {
        type = type.toLowerCase();
        Material potionMaterial;

        switch (type) {
            case "normal" -> potionMaterial = Material.POTION;
            case "splash" -> potionMaterial = Material.SPLASH_POTION;
            case "lingering" -> potionMaterial = Material.LINGERING_POTION;
            default -> {
                player.sendMessage("§cInvalid type! Use: normal, splash, or lingering");
                return;
            }
        }

        if (amplifier < 0 || amplifier > 255) {
            player.sendMessage("§cAmplifier must be between 0 and 255!");
            return;
        }

        if (duration < 1) {
            player.sendMessage("§cDuration must be at least 1 second!");
            return;
        }

        if (amount < 1) {
            player.sendMessage("§cAmount must be at least 1!");
            return;
        }

        int maxStackSize = potionMaterial.getMaxStackSize();
        int emptySlots = 0;
        int partialStackSpace = 0;

        for (ItemStack invItem : player.getInventory().getStorageContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) {
                emptySlots++;
            } else if (invItem.getType() == potionMaterial && invItem.getAmount() < maxStackSize) {
                partialStackSpace += (maxStackSize - invItem.getAmount());
            }
        }

        int maxCanFit = (emptySlots * maxStackSize) + partialStackSpace;

        if (amount > maxCanFit) {
            player.sendMessage("§cCannot give " + amount + " potions! Only " + maxCanFit + " can fit in your inventory.");
            return;
        }

        for (int i = 0; i < amount; i++) {
            ItemStack potion = new ItemStack(potionMaterial, 1);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();

            meta.addCustomEffect(new PotionEffect(potionType, duration * 20, amplifier), true);

            potion.setItemMeta(meta);
            player.getInventory().addItem(potion);
        }

        player.sendMessage("§aGave you §e" + amount + "x §d" + type + " " + formatPotionName(potionType.getName()) + " " + (amplifier + 1) + " §a(" + formatDuration(duration) + ")");
    }

    private String formatPotionName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }

    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return secs > 0 ? minutes + "m " + secs + "s" : minutes + "m";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        }
    }
}