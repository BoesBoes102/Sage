package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CommandAlias("openinv|invsee|viewinv")
@Description("Open another player's inventory")
@CommandPermission("sage.openinv")
public class OpenInventoryCommand extends BaseCommand implements Listener {
    private static final int GUI_SIZE = 54;
    private static final long LOCAL_EDIT_REFRESH_DELAY_MS = 300L;

    private final Sage plugin;
    private final Map<UUID, InventorySession> sessionsByViewer = new HashMap<>();
    private final Map<UUID, Set<UUID>> viewersByTarget = new HashMap<>();

    public OpenInventoryCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onCommand(Player viewer, String[] args) {
        if (args.length < 1) {
            viewer.sendMessage(ChatColor.RED + "Usage: /openinv <player>");
            return;
        }

        Player onlineTarget = Bukkit.getPlayer(args[0]);
        UUID targetUUID;
        String targetName;

        if (onlineTarget != null) {
            targetUUID = onlineTarget.getUniqueId();
            targetName = onlineTarget.getName();
        } else {
            targetUUID = OfflinePlayerDataManager.getPlayerUUID(args[0]);
            if (targetUUID == null) {
                viewer.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetUUID);
            targetName = offlineTarget.getName() != null ? offlineTarget.getName() : args[0];
        }

        if (targetUUID.equals(viewer.getUniqueId())) {
            viewer.sendMessage(ChatColor.RED + "You cannot open your own inventory!");
            return;
        }

        try {
            openInventory(viewer, targetUUID, targetName);
            viewer.sendMessage(ChatColor.GREEN + "Opened inventory of " + ChatColor.YELLOW + targetName);
        } catch (Exception e) {
            viewer.sendMessage(ChatColor.RED + "Error opening inventory!");
            e.printStackTrace();
        }
    }

    private void openInventory(Player viewer, UUID targetUUID, String targetName) throws Exception {
        UUID viewerUUID = viewer.getUniqueId();
        endSession(viewerUUID, true);

        Inventory inventory = Bukkit.createInventory(null, GUI_SIZE, getTitle(targetName));
        InventorySession session = new InventorySession(viewerUUID, targetUUID, targetName, inventory);
        loadSourceIntoSession(session, true);

        viewer.openInventory(inventory);
        sessionsByViewer.put(viewerUUID, session);
        viewersByTarget.computeIfAbsent(targetUUID, ignored -> new HashSet<>()).add(viewerUUID);
        startRefreshTask(session);
    }

    private void startRefreshTask(InventorySession session) {
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player viewer = Bukkit.getPlayer(session.viewerUUID);
            if (viewer == null || !viewer.isOnline()) {
                endSession(session.viewerUUID, true);
                return;
            }

            InventorySession currentSession = sessionsByViewer.get(session.viewerUUID);
            if (currentSession != session || !isViewingSession(viewer, session)) {
                endSession(session.viewerUUID, true);
                return;
            }

            Player target = Bukkit.getPlayer(session.targetUUID);
            if (target != null && !session.isWaitingForLocalEdit()) {
                loadPlayerIntoGui(session.inventory, target);
            }
        }, 1L, 2L).getTaskId();

        session.taskId = taskId;
    }

    private boolean isViewingSession(Player viewer, InventorySession session) {
        return viewer.getOpenInventory() != null
                && viewer.getOpenInventory().getTopInventory().equals(session.inventory);
    }

    private void loadSourceIntoSession(InventorySession session, boolean preferOnline) throws Exception {
        Player target = preferOnline ? Bukkit.getPlayer(session.targetUUID) : null;
        if (target != null) {
            loadPlayerIntoGui(session.inventory, target);
            return;
        }

        ItemStack[] contents = OfflinePlayerDataManager.loadInventoryFromFile(session.targetUUID);
        ItemStack[] armor = OfflinePlayerDataManager.loadArmorFromFile(session.targetUUID);
        ItemStack offhand = OfflinePlayerDataManager.loadOffhandFromFile(session.targetUUID);
        loadDataIntoGui(session.inventory, contents, armor, offhand);
    }

    private void loadPlayerIntoGui(Inventory gui, Player target) {
        loadDataIntoGui(
                gui,
                target.getInventory().getContents(),
                target.getInventory().getArmorContents(),
                target.getInventory().getItemInOffHand()
        );
    }

    private void loadDataIntoGui(Inventory gui, ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
        gui.clear();

        for (int i = 9; i < 36 && i < contents.length; i++) {
            gui.setItem(i - 9, copyItem(contents[i]));
        }

        for (int i = 0; i < 9 && i < contents.length; i++) {
            gui.setItem(27 + i, copyItem(contents[i]));
        }

        if (armor != null && armor.length >= 4) {
            gui.setItem(36, copyItem(armor[3]));
            gui.setItem(37, copyItem(armor[2]));
            gui.setItem(38, copyItem(armor[1]));
            gui.setItem(39, copyItem(armor[0]));
        }

        gui.setItem(40, copyItem(offhand));
        fillLockedSlots(gui);
    }

    private void fillLockedSlots(Inventory gui) {
        ItemStack divider = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        for (int slot = 41; slot < GUI_SIZE; slot++) {
            gui.setItem(slot, divider);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        InventorySession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(session.inventory)) {
            if (!isEditableSlot(event.getRawSlot())) {
                event.setCancelled(true);
                return;
            }

            session.markLocalEdit();
            persistAndRefreshAfterInventoryEvent(session.viewerUUID);
        } else if (event.isShiftClick()) {
            event.setCancelled(true);
            moveShiftClickedItemIntoSession(event);
            session.markLocalEdit();
            persistAndRefreshAfterInventoryEvent(session.viewerUUID);
        }
    }

    private void moveShiftClickedItemIntoSession(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemStack remaining = clicked.clone();
        Inventory topInventory = event.getView().getTopInventory();

        for (int slot = 0; slot <= 40 && remaining.getAmount() > 0; slot++) {
            ItemStack existing = topInventory.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR || !existing.isSimilar(remaining)) continue;

            int maxStackSize = existing.getMaxStackSize();
            int space = maxStackSize - existing.getAmount();
            if (space <= 0) continue;

            int moved = Math.min(space, remaining.getAmount());
            existing.setAmount(existing.getAmount() + moved);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        for (int slot = 0; slot <= 40 && remaining.getAmount() > 0; slot++) {
            ItemStack existing = topInventory.getItem(slot);
            if (existing != null && existing.getType() != Material.AIR) continue;

            int moved = Math.min(remaining.getMaxStackSize(), remaining.getAmount());
            ItemStack placed = remaining.clone();
            placed.setAmount(moved);
            topInventory.setItem(slot, placed);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        event.setCurrentItem(remaining.getAmount() > 0 ? remaining : null);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        InventorySession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        boolean touchesEditableSlot = false;
        for (int slot : event.getRawSlots()) {
            if (slot < GUI_SIZE && !isEditableSlot(slot)) {
                event.setCancelled(true);
                return;
            }

            if (slot < GUI_SIZE) {
                touchesEditableSlot = true;
            }
        }

        if (touchesEditableSlot) {
            session.markLocalEdit();
            persistAndRefreshAfterInventoryEvent(session.viewerUUID);
        }
    }

    private boolean isEditableSlot(int rawSlot) {
        return rawSlot >= 0 && rawSlot <= 40;
    }

    private void persistAndRefreshAfterInventoryEvent(UUID viewerUUID) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            InventorySession session = sessionsByViewer.get(viewerUUID);
            if (session == null) return;

            try {
                persistSession(session);
                refreshOtherViewers(session);
            } catch (Exception e) {
                Player viewer = Bukkit.getPlayer(viewerUUID);
                if (viewer != null) {
                    viewer.sendMessage(ChatColor.RED + "Error saving inventory data!");
                }
                e.printStackTrace();
            }
        });
    }

    private void persistSession(InventorySession session) throws Exception {
        InventoryData data = extractInventoryData(session.inventory);
        Player target = Bukkit.getPlayer(session.targetUUID);

        if (target != null) {
            target.getInventory().setContents(copyContents(data.contents, 36));
            target.getInventory().setArmorContents(copyContents(data.armor, 4));
            target.getInventory().setItemInOffHand(copyItem(data.offhand));
            target.updateInventory();
        }

        saveOfflineInventory(session.targetUUID, data);
    }

    private void saveOfflineInventory(UUID targetUUID, InventoryData data) throws Exception {
        OfflinePlayerDataManager.saveInventoryToFile(targetUUID, data.contents);
        OfflinePlayerDataManager.saveArmorToFile(targetUUID, data.armor);
        OfflinePlayerDataManager.saveOffhandToFile(targetUUID, data.offhand);
    }

    private void refreshOtherViewers(InventorySession changedSession) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(changedSession.targetUUID);
        if (viewerUUIDs == null) return;

        InventoryData data = extractInventoryData(changedSession.inventory);
        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            if (viewerUUID.equals(changedSession.viewerUUID)) continue;

            InventorySession otherSession = sessionsByViewer.get(viewerUUID);
            if (otherSession != null && !otherSession.isWaitingForLocalEdit()) {
                loadDataIntoGui(otherSession.inventory, data.contents, data.armor, data.offhand);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;
        endSession(viewer.getUniqueId(), true);
    }

    public void prepareForPlayerJoin(Player player) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(player.getUniqueId());
        if (viewerUUIDs == null) return;

        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            InventorySession session = sessionsByViewer.get(viewerUUID);
            if (session == null) continue;

            try {
                saveOfflineInventory(session.targetUUID, extractInventoryData(session.inventory));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> refreshTargetViewersFromPlayer(player));
    }

    private void refreshTargetViewersFromPlayer(Player player) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(player.getUniqueId());
        if (viewerUUIDs == null) return;

        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            InventorySession session = sessionsByViewer.get(viewerUUID);
            if (session != null && !session.isWaitingForLocalEdit()) {
                loadPlayerIntoGui(session.inventory, player);
            }
        }
    }

    public void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        endSession(playerUUID, true);

        try {
            InventoryData data = getMostRecentTargetData(player);
            saveOfflineInventory(playerUUID, data);

            Set<UUID> viewerUUIDs = viewersByTarget.get(playerUUID);
            if (viewerUUIDs != null) {
                for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
                    InventorySession session = sessionsByViewer.get(viewerUUID);
                    if (session != null && !session.isWaitingForLocalEdit()) {
                        loadDataIntoGui(session.inventory, data.contents, data.armor, data.offhand);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InventoryData getMostRecentTargetData(Player target) {
        InventoryData data = new InventoryData(
                copyContents(target.getInventory().getContents(), 36),
                copyContents(target.getInventory().getArmorContents(), 4),
                copyItem(target.getInventory().getItemInOffHand())
        );

        Set<UUID> viewerUUIDs = viewersByTarget.get(target.getUniqueId());
        if (viewerUUIDs == null) {
            return data;
        }

        InventorySession newestEditedSession = null;
        for (UUID viewerUUID : viewerUUIDs) {
            InventorySession session = sessionsByViewer.get(viewerUUID);
            if (session == null || session.lastLocalEditAt == 0L) continue;

            if (newestEditedSession == null || session.lastLocalEditAt > newestEditedSession.lastLocalEditAt) {
                newestEditedSession = session;
            }
        }

        return newestEditedSession != null ? extractInventoryData(newestEditedSession.inventory) : data;
    }

    public void closeInventoriesForPlayer(UUID playerUUID) {
        handleTargetTransition(playerUUID);
    }

    public void handleTargetTransition(UUID playerUUID) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(playerUUID);
        if (viewerUUIDs == null) return;

        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            InventorySession session = sessionsByViewer.get(viewerUUID);
            if (session == null) continue;

            try {
                persistSession(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void endSession(UUID viewerUUID, boolean save) {
        InventorySession session = sessionsByViewer.remove(viewerUUID);
        if (session == null) return;

        if (session.taskId != -1) {
            Bukkit.getScheduler().cancelTask(session.taskId);
        }

        Set<UUID> viewers = viewersByTarget.get(session.targetUUID);
        if (viewers != null) {
            viewers.remove(viewerUUID);
            if (viewers.isEmpty()) {
                viewersByTarget.remove(session.targetUUID);
            }
        }

        if (save) {
            try {
                persistSession(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cleanup() {
        for (UUID viewerUUID : new ArrayList<>(sessionsByViewer.keySet())) {
            endSession(viewerUUID, true);
        }
        sessionsByViewer.clear();
        viewersByTarget.clear();
    }

    private InventoryData extractInventoryData(Inventory gui) {
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 27; i++) {
            contents[i + 9] = copyItem(gui.getItem(i));
        }

        for (int i = 0; i < 9; i++) {
            contents[i] = copyItem(gui.getItem(27 + i));
        }

        ItemStack[] armor = new ItemStack[4];
        armor[3] = copyItem(gui.getItem(36));
        armor[2] = copyItem(gui.getItem(37));
        armor[1] = copyItem(gui.getItem(38));
        armor[0] = copyItem(gui.getItem(39));

        return new InventoryData(contents, armor, copyItem(gui.getItem(40)));
    }

    private ItemStack[] copyContents(ItemStack[] contents, int size) {
        ItemStack[] copy = new ItemStack[size];
        for (int i = 0; i < copy.length && i < contents.length; i++) {
            copy[i] = copyItem(contents[i]);
        }
        return copy;
    }

    private ItemStack copyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        return item.clone();
    }

    private String getTitle(String targetName) {
        return ChatColor.DARK_GRAY + targetName + "'s Inventory";
    }

    private static class InventoryData {
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final ItemStack offhand;

        private InventoryData(ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
            this.contents = contents;
            this.armor = armor;
            this.offhand = offhand;
        }
    }

    private static class InventorySession {
        private final UUID viewerUUID;
        private final UUID targetUUID;
        private final String targetName;
        private final Inventory inventory;
        private int taskId = -1;
        private long skipRefreshUntil = 0L;
        private long lastLocalEditAt = 0L;

        private InventorySession(UUID viewerUUID, UUID targetUUID, String targetName, Inventory inventory) {
            this.viewerUUID = viewerUUID;
            this.targetUUID = targetUUID;
            this.targetName = targetName;
            this.inventory = inventory;
        }

        private void markLocalEdit() {
            lastLocalEditAt = System.currentTimeMillis();
            skipRefreshUntil = lastLocalEditAt + LOCAL_EDIT_REFRESH_DELAY_MS;
        }

        private boolean isWaitingForLocalEdit() {
            return System.currentTimeMillis() < skipRefreshUntil;
        }
    }
}
