package com.boes.sage.StaffCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import com.boes.sage.Sage;
import com.boes.sage.gui.KitGUI;
import com.boes.sage.managers.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandAlias("kit")
public class KitCommand extends BaseCommand {
    private final Sage plugin;
    private final KitManager kitManager;
    private final Map<UUID, String> kitBeingCreated = new HashMap<>();
    private final Map<UUID, String> kitBeingDuration = new HashMap<>();

    public KitCommand(Sage plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
    }

    @Default
    @Conditions("player-only")
    public void onDefault(Player player) {
        KitGUI gui = new KitGUI(plugin, player);
        gui.open();
    }

    @Subcommand("claim")
    @Conditions("player-only")
    @CommandCompletion("@kits")
    public void onClaim(Player player, String kitName) {
        if (!kitManager.kitExists(kitName)) {
            player.sendMessage("§cKit '" + kitName + "' does not exist!");
            return;
        }

        if (!player.hasPermission("sage.kit." + kitName)) {
            player.sendMessage("§cYou don't have permission to claim this kit!");
            return;
        }

        if (!kitManager.canClaimKit(player, kitName)) {
            long remaining = kitManager.getKitCooldownRemaining(player, kitName);
            player.sendMessage("§cYou can claim this kit again in: §b" + formatTime(remaining));
            return;
        }

        kitManager.claimKit(player, kitName);
        player.sendMessage("§a§lKit '" + kitName + "' claimed!");
    }

    @Subcommand("create")
    @Conditions("player-only")
    @CommandPermission("sage.kit.create")
    public void onCreate(Player player, String kitName, String duration) {
        if (kitManager.kitExists(kitName)) {
            player.sendMessage("§cKit '" + kitName + "' already exists!");
            return;
        }

        if (!duration.matches(".*[smhd]")) {
            player.sendMessage("§cInvalid duration format! Use: <number>s/m/h/d (e.g., 24h, 30m)");
            return;
        }

        kitBeingCreated.put(player.getUniqueId(), kitName);
        kitBeingDuration.put(player.getUniqueId(), duration);

        player.sendMessage("§a§lKit creation started!");
        player.sendMessage("§7Add items to your inventory, then run: §e/kit confirmcreate");
        player.sendMessage("§7Or cancel with: §e/kit cancelcreate");
    }

    @Subcommand("delete")
    @CommandPermission("sage.kit.delete")
    @CommandCompletion("@kits")
    public void onDelete(Player player, String kitName) {
        if (!kitManager.kitExists(kitName)) {
            player.sendMessage("§cKit '" + kitName + "' does not exist!");
            return;
        }

        kitManager.deleteKit(kitName);
        player.sendMessage("§a§lKit '" + kitName + "' deleted!");
    }

    @Subcommand("gui")
    @Conditions("player-only")
    public void onGui(Player player) {
        KitGUI gui = new KitGUI(plugin, player);
        gui.open();
    }

    @Subcommand("confirmcreate")
    @Conditions("player-only")
    @CommandPermission("sage.kit.create")
    public void onConfirmCreate(Player player) {
        UUID uuid = player.getUniqueId();

        if (!kitBeingCreated.containsKey(uuid)) {
            player.sendMessage("§cNo kit creation in progress!");
            return;
        }

        String kitName = kitBeingCreated.get(uuid);
        String duration = kitBeingDuration.get(uuid);

        ItemStack[] items = player.getInventory().getContents();
        kitManager.createKit(kitName, duration, items);

        kitBeingCreated.remove(uuid);
        kitBeingDuration.remove(uuid);

        player.sendMessage("§a§lKit '" + kitName + "' created successfully!");
        player.sendMessage("§7Duration: §b" + duration);

        player.getInventory().clear();
    }

    @Subcommand("cancelcreate")
    @Conditions("player-only")
    @CommandPermission("sage.kit.create")
    public void onCancelCreate(Player player) {
        UUID uuid = player.getUniqueId();

        if (!kitBeingCreated.containsKey(uuid)) {
            player.sendMessage("§cNo kit creation in progress!");
            return;
        }

        kitBeingCreated.remove(uuid);
        kitBeingDuration.remove(uuid);

        player.sendMessage("§c§lKit creation cancelled!");
    }

    private String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}