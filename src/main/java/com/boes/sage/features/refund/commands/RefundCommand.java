package com.boes.sage.features.refund.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.features.refund.data.RefundSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@CommandAlias("refund")
@Description("Review and claim saved player refund snapshots")
public class RefundCommand extends BaseCommand implements Listener {
    private static final String ADMIN_TITLE_PREFIX = ChatColor.DARK_GRAY + "Refunds: " + ChatColor.YELLOW;
    private static final String DETAIL_TITLE_PREFIX = ChatColor.DARK_GRAY + "Refund: " + ChatColor.YELLOW;
    private static final String CLAIM_TITLE = ChatColor.DARK_GRAY + "Your Refunds";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Sage plugin;
    private final Map<UUID, AdminMainContext> adminMainContexts = new HashMap<>();
    private final Map<UUID, AdminDetailContext> adminDetailContexts = new HashMap<>();
    private final Map<UUID, Map<Integer, String>> claimSlotIds = new HashMap<>();

    public RefundCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onCommand(Player player, @Optional String targetName) {
        if (targetName == null || targetName.isBlank()) {
            openClaimGui(player);
            return;
        }

        if (!player.hasPermission("sage.refund.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view player refunds!");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player has never joined!");
            return;
        }

        openAdminGui(player, target.getUniqueId(), target.getName() == null ? targetName : target.getName());
    }

    private void openAdminGui(Player viewer, UUID targetUuid, String targetName) {
        List<RefundSnapshot> snapshots = plugin.getRefundService().getRefunds(targetUuid);
        Inventory gui = Bukkit.createInventory(null, 54, ADMIN_TITLE_PREFIX + targetName);
        Map<Integer, String> slotIds = new HashMap<>();

        int slot = 0;
        for (RefundSnapshot snapshot : snapshots) {
            if (slot >= 45) {
                break;
            }

            gui.setItem(slot, createSnapshotPaper(snapshot, true));
            slotIds.put(slot, snapshot.getId());
            slot++;
        }

        for (int i = 45; i < 54; i++) {
            gui.setItem(i, createGlassPane());
        }

        if (snapshots.isEmpty()) {
            gui.setItem(22, createButton(Material.BARRIER, ChatColor.RED + "No saved refunds", List.of(
                    ChatColor.GRAY + "This player has no unexpired refund records."
            )));
        }

        adminDetailContexts.remove(viewer.getUniqueId());
        claimSlotIds.remove(viewer.getUniqueId());
        viewer.openInventory(gui);
        adminMainContexts.put(viewer.getUniqueId(), new AdminMainContext(targetUuid, targetName, slotIds));
    }

    private void openDetailGui(Player viewer, UUID targetUuid, String targetName, String refundId) {
        RefundSnapshot snapshot = plugin.getRefundService().getRefund(targetUuid, refundId);
        if (snapshot == null) {
            viewer.sendMessage(ChatColor.RED + "That refund has expired or was deleted.");
            openAdminGui(viewer, targetUuid, targetName);
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, DETAIL_TITLE_PREFIX + targetName);
        ItemStack[] inventory = snapshot.getInventory();
        for (int i = 0; i < 36 && i < inventory.length; i++) {
            gui.setItem(i, cloneItem(inventory[i]));
        }

        ItemStack[] armor = snapshot.getArmor();
        gui.setItem(36, armorItem(armor, 3, Material.LEATHER_HELMET, "Helmet"));
        gui.setItem(37, armorItem(armor, 2, Material.LEATHER_CHESTPLATE, "Chestplate"));
        gui.setItem(38, armorItem(armor, 1, Material.LEATHER_LEGGINGS, "Leggings"));
        gui.setItem(39, armorItem(armor, 0, Material.LEATHER_BOOTS, "Boots"));
        gui.setItem(40, snapshot.getOffhand() == null ? createSlotPlaceholder(Material.SHIELD, "Offhand") : snapshot.getOffhand().clone());

        gui.setItem(41, createSnapshotPaper(snapshot, false));
        for (int i = 42; i < 45; i++) {
            gui.setItem(i, createGlassPane());
        }

        gui.setItem(45, createButton(Material.GREEN_WOOL, ChatColor.GREEN + "Send claimable refund", List.of(
                ChatColor.GRAY + "Marks this refund for the player.",
                ChatColor.GRAY + "They can collect it with " + ChatColor.YELLOW + "/refund" + ChatColor.GRAY + "."
        )));
        gui.setItem(46, createButton(Material.RED_WOOL, ChatColor.RED + "Override player inventory", List.of(
                ChatColor.GRAY + "Requires the player to be online.",
                ChatColor.GRAY + "Replaces inventory, armor, offhand, and XP."
        )));
        gui.setItem(47, createButton(Material.BEACON, ChatColor.AQUA + "Teleport to saved location", List.of(
                ChatColor.GRAY + "Only works while you are in creative mode."
        )));
        for (int i = 48; i < 53; i++) {
            gui.setItem(i, createGlassPane());
        }
        gui.setItem(53, createButton(Material.BLACK_WOOL, ChatColor.WHITE + "Back", List.of(
                ChatColor.GRAY + "Return to all saved refunds."
        )));

        adminMainContexts.remove(viewer.getUniqueId());
        claimSlotIds.remove(viewer.getUniqueId());
        viewer.openInventory(gui);
        adminDetailContexts.put(viewer.getUniqueId(), new AdminDetailContext(targetUuid, targetName, refundId));
    }

    private void openClaimGui(Player player) {
        List<RefundSnapshot> snapshots = plugin.getRefundService().getPendingRefunds(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(null, 54, CLAIM_TITLE);
        Map<Integer, String> slotIds = new HashMap<>();

        int slot = 0;
        for (RefundSnapshot snapshot : snapshots) {
            if (slot >= 45) {
                break;
            }

            gui.setItem(slot, createSnapshotPaper(snapshot, false));
            slotIds.put(slot, snapshot.getId());
            slot++;
        }

        for (int i = 45; i < 54; i++) {
            gui.setItem(i, createGlassPane());
        }

        if (snapshots.isEmpty()) {
            gui.setItem(22, createButton(Material.BARRIER, ChatColor.RED + "No pending refunds", List.of(
                    ChatColor.GRAY + "Staff have not sent you any claimable refunds."
            )));
        }

        adminMainContexts.remove(player.getUniqueId());
        adminDetailContexts.remove(player.getUniqueId());
        player.openInventory(gui);
        claimSlotIds.put(player.getUniqueId(), slotIds);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        UUID viewerUuid = player.getUniqueId();
        String title = event.getView().getTitle();
        boolean refundGui = (title.startsWith(ADMIN_TITLE_PREFIX) && adminMainContexts.containsKey(viewerUuid))
                || (title.startsWith(DETAIL_TITLE_PREFIX) && adminDetailContexts.containsKey(viewerUuid))
                || (title.equals(CLAIM_TITLE) && claimSlotIds.containsKey(viewerUuid));

        if (!refundGui) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        if (title.startsWith(ADMIN_TITLE_PREFIX) && adminMainContexts.containsKey(viewerUuid)) {
            handleAdminMainClick(player, event);
            return;
        }

        if (title.startsWith(DETAIL_TITLE_PREFIX) && adminDetailContexts.containsKey(viewerUuid)) {
            handleAdminDetailClick(player, event);
            return;
        }

        if (title.equals(CLAIM_TITLE) && claimSlotIds.containsKey(viewerUuid)) {
            handleClaimClick(player, event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        UUID viewerUuid = event.getWhoClicked().getUniqueId();
        String title = event.getView().getTitle();
        boolean refundGui = (title.startsWith(ADMIN_TITLE_PREFIX) && adminMainContexts.containsKey(viewerUuid))
                || (title.startsWith(DETAIL_TITLE_PREFIX) && adminDetailContexts.containsKey(viewerUuid))
                || (title.equals(CLAIM_TITLE) && claimSlotIds.containsKey(viewerUuid));

        if (!refundGui) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void handleAdminMainClick(Player viewer, InventoryClickEvent event) {
        AdminMainContext context = adminMainContexts.get(viewer.getUniqueId());
        String refundId = context.slotIds.get(event.getRawSlot());
        if (refundId == null) {
            return;
        }

        if (event.getClick() == ClickType.RIGHT) {
            RefundSnapshot snapshot = plugin.getRefundService().getRefund(context.targetUuid, refundId);
            teleportIfCreative(viewer, snapshot);
            return;
        }

        if (event.getClick().isLeftClick()) {
            openDetailGui(viewer, context.targetUuid, context.targetName, refundId);
        }
    }

    private void handleAdminDetailClick(Player viewer, InventoryClickEvent event) {
        AdminDetailContext context = adminDetailContexts.get(viewer.getUniqueId());
        RefundSnapshot snapshot = plugin.getRefundService().getRefund(context.targetUuid, context.refundId);
        if (snapshot == null) {
            viewer.sendMessage(ChatColor.RED + "That refund has expired or was deleted.");
            openAdminGui(viewer, context.targetUuid, context.targetName);
            return;
        }

        int slot = event.getRawSlot();
        if (slot == 45) {
            plugin.getRefundService().markPending(snapshot, true);
            viewer.sendMessage(ChatColor.GREEN + "Sent a claimable refund to " + context.targetName + ".");
            Player target = Bukkit.getPlayer(context.targetUuid);
            if (target != null) {
                target.sendMessage(ChatColor.GREEN + "A staff member sent you a refund. Use " + ChatColor.YELLOW + "/refund" + ChatColor.GREEN + " to claim it.");
            }
            return;
        }

        if (slot == 46) {
            Player target = Bukkit.getPlayer(context.targetUuid);
            if (target == null) {
                viewer.sendMessage(ChatColor.RED + "That player must be online before their inventory can be overridden.");
                return;
            }

            plugin.getRefundService().applyOverrideRefund(target, snapshot);
            viewer.sendMessage(ChatColor.GREEN + "Overrode " + target.getName() + "'s inventory and XP from the saved refund.");
            target.sendMessage(ChatColor.GREEN + "Your inventory and XP were restored by staff.");
            return;
        }

        if (slot == 47) {
            teleportIfCreative(viewer, snapshot);
            return;
        }

        if (slot == 53) {
            openAdminGui(viewer, context.targetUuid, context.targetName);
        }
    }

    private void handleClaimClick(Player player, InventoryClickEvent event) {
        Map<Integer, String> slots = claimSlotIds.get(player.getUniqueId());
        String refundId = slots.get(event.getRawSlot());
        if (refundId == null || !event.getClick().isLeftClick()) {
            return;
        }

        RefundSnapshot snapshot = plugin.getRefundService().getRefund(player.getUniqueId(), refundId);
        if (snapshot == null || !snapshot.isPendingClaim()) {
            player.sendMessage(ChatColor.RED + "That refund is no longer available.");
            openClaimGui(player);
            return;
        }

        plugin.getRefundService().applyAdditiveRefund(player, snapshot);
        player.sendMessage(ChatColor.GREEN + "Refund claimed. Items that did not fit were dropped at your feet.");
        openClaimGui(player);
    }

    private void teleportIfCreative(Player viewer, RefundSnapshot snapshot) {
        if (snapshot == null || snapshot.getLocation() == null || snapshot.getLocation().getWorld() == null) {
            viewer.sendMessage(ChatColor.RED + "This refund does not have a valid saved location.");
            return;
        }

        if (viewer.getGameMode() != GameMode.CREATIVE) {
            viewer.sendMessage(ChatColor.RED + "You must be in creative mode to teleport to refund locations.");
            return;
        }

        plugin.getRefundService().teleportToSavedLocation(viewer, snapshot);
        viewer.sendMessage(ChatColor.GREEN + "Teleported to the saved refund location.");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        adminMainContexts.remove(uuid);
        adminDetailContexts.remove(uuid);
        claimSlotIds.remove(uuid);
    }

    private ItemStack createSnapshotPaper(RefundSnapshot snapshot, boolean adminActions) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Saved: " + ChatColor.WHITE + formatDate(snapshot.getCreatedAt()));
        lore.add(ChatColor.GRAY + "Expires in: " + ChatColor.YELLOW + formatRemaining(snapshot.getExpiresAt() - System.currentTimeMillis()));
        lore.add(ChatColor.GRAY + "Deleted at: " + ChatColor.WHITE + formatDate(snapshot.getExpiresAt()));
        lore.add(ChatColor.GRAY + "Reason: " + ChatColor.AQUA + formatReason(snapshot.getSaveReason()));
        if (snapshot.getDeathReason() != null && !snapshot.getDeathReason().isBlank()) {
            lore.add(ChatColor.GRAY + "Death: " + ChatColor.WHITE + snapshot.getDeathReason());
        }
        lore.add(ChatColor.GRAY + "XP refund: " + ChatColor.GREEN + snapshot.getTotalExperience()
                + ChatColor.GRAY + " total XP (level " + snapshot.getLevel() + ")");
        if (snapshot.isPendingClaim()) {
            lore.add(ChatColor.GREEN + "Pending player claim");
        }
        lore.add("");
        if (adminActions) {
            lore.add(ChatColor.YELLOW + "Left click " + ChatColor.GRAY + "to open inventory copy");
            lore.add(ChatColor.YELLOW + "Right click " + ChatColor.GRAY + "to teleport in creative mode");
        } else {
            lore.add(ChatColor.YELLOW + "Left click " + ChatColor.GRAY + "to claim");
        }

        return createButton(Material.PAPER, ChatColor.WHITE + formatReason(snapshot.getSaveReason()) + " refund", lore);
    }

    private ItemStack armorItem(ItemStack[] armor, int index, Material fallback, String name) {
        if (armor != null && index >= 0 && index < armor.length && armor[index] != null && armor[index].getType() != Material.AIR) {
            return armor[index].clone();
        }
        return createSlotPlaceholder(fallback, name);
    }

    private ItemStack createSlotPlaceholder(Material material, String name) {
        return createButton(material, ChatColor.DARK_GRAY + name + " slot", List.of(ChatColor.GRAY + "No item saved here."));
    }

    private ItemStack createGlassPane() {
        return createButton(Material.BLACK_STAINED_GLASS_PANE, ChatColor.DARK_GRAY.toString(), List.of());
    }

    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack cloneItem(ItemStack item) {
        return item == null || item.getType() == Material.AIR ? null : item.clone();
    }

    private String formatDate(long millis) {
        return DATE_FORMAT.format(Instant.ofEpochMilli(millis));
    }

    private String formatReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Unknown";
        }

        String lower = reason.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String formatRemaining(long millis) {
        if (millis <= 0) {
            return "expired";
        }

        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    private record AdminMainContext(UUID targetUuid, String targetName, Map<Integer, String> slotIds) {
    }

    private record AdminDetailContext(UUID targetUuid, String targetName, String refundId) {
    }
}
