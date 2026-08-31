package com.boes.sage.features.openinv;

import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class OpenInventoryService {
    private static final int GUI_SIZE = 54;
    private static final long LOCAL_EDIT_REFRESH_DELAY_MS = 300L;

    private static final int CURSOR_SLOT = 41;
    private static final int[] MATRIX_SLOTS = {42, 43, 51, 52};
    private static final int MATRIX_SLOT_COUNT = MATRIX_SLOTS.length;
    private static final int[] FILLER_SLOTS = {44, 45, 46, 47, 48, 49, 50};
    private static final int RESULT_SLOT = 53;

    private static final int HELMET_SLOT = 36;
    private static final int CHESTPLATE_SLOT = 37;
    private static final int LEGGINGS_SLOT = 38;
    private static final int BOOTS_SLOT = 39;
    private static final int OFFHAND_SLOT = 40;

    private static final int[] HOTBAR_SLOTS = {27, 28, 29, 30, 31, 32, 33, 34, 35};
    private static final int[] MAIN_INVENTORY_SLOTS = buildRange(0, 27);

    private final Sage plugin;
    private final Map<UUID, InventorySession> sessionsByViewer = new HashMap<>();
    private final Map<UUID, Set<UUID>> viewersByTarget = new HashMap<>();
    private final NamespacedKey placeholderKey;
    private final Map<UUID, CompletableFuture<Void>> pendingSaves = new ConcurrentHashMap<>();
    private final Executor asyncExecutor;

    public OpenInventoryService(Sage plugin) {
        this.plugin = plugin;
        this.placeholderKey = new NamespacedKey(plugin, "openinv_placeholder");
        this.asyncExecutor = runnable -> Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    private static int[] buildRange(int startInclusive, int endExclusive) {
        int[] range = new int[endExclusive - startInclusive];
        for (int i = 0; i < range.length; i++) {
            range[i] = startInclusive + i;
        }
        return range;
    }

    public void openInventory(Player viewer, UUID targetUUID, String targetName) throws Exception {
        UUID viewerUUID = viewer.getUniqueId();
        endSession(viewerUUID, true);

        Inventory inventory = Bukkit.createInventory(null, GUI_SIZE, getTitle(targetName));
        InventorySession session = new InventorySession(viewerUUID, targetUUID, targetName, inventory);

        Player onlineTarget = Bukkit.getPlayer(targetUUID);
        if (onlineTarget != null) {
            loadPlayerIntoGui(inventory, onlineTarget);
            finishOpeningInventory(viewer, session);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ItemStack[] contents = OfflinePlayerDataManager.loadInventoryFromFile(targetUUID);
                ItemStack[] armor = OfflinePlayerDataManager.loadArmorFromFile(targetUUID);
                ItemStack offhand = OfflinePlayerDataManager.loadOffhandFromFile(targetUUID);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    loadDataIntoGui(inventory, contents, armor, offhand, null, null, null, false);
                    finishOpeningInventory(viewer, session);
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (viewer.isOnline()) {
                        viewer.sendMessage(ChatColor.RED + "Error loading offline inventory data!");
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    private void finishOpeningInventory(Player viewer, InventorySession session) {
        viewer.openInventory(session.inventory);
        sessionsByViewer.put(session.viewerUUID, session);
        viewersByTarget.computeIfAbsent(session.targetUUID, ignored -> new HashSet<>()).add(session.viewerUUID);
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

    private void loadPlayerIntoGui(Inventory gui, Player target) {
        ItemStack[] matrix = null;
        ItemStack result = null;

        InventoryView openView = target.getOpenInventory();
        if (openView != null && openView.getTopInventory() instanceof CraftingInventory craftingInventory) {
            matrix = craftingInventory.getMatrix();
            result = craftingInventory.getResult();
        }

        loadDataIntoGui(
                gui,
                target.getInventory().getContents(),
                target.getInventory().getArmorContents(),
                target.getInventory().getItemInOffHand(),
                matrix,
                result,
                target.getItemOnCursor(),
                true
        );
    }

    private void loadDataIntoGui(Inventory gui, ItemStack[] contents, ItemStack[] armor, ItemStack offhand, boolean targetOnline) {
        loadDataIntoGui(gui, contents, armor, offhand, null, null, null, targetOnline);
    }

    private void loadDataIntoGui(Inventory gui, ItemStack[] contents, ItemStack[] armor, ItemStack offhand,
                                  ItemStack[] craftingMatrix, ItemStack craftingResult, ItemStack cursorItem, boolean targetOnline) {
        gui.clear();

        for (int i = 9; i < 36 && i < contents.length; i++) {
            gui.setItem(i - 9, copyItem(contents[i]));
        }

        for (int i = 0; i < 9 && i < contents.length; i++) {
            gui.setItem(27 + i, copyItem(contents[i]));
        }

        ItemStack helmet = armor != null && armor.length > 3 ? armor[3] : null;
        ItemStack chestplate = armor != null && armor.length > 2 ? armor[2] : null;
        ItemStack leggings = armor != null && armor.length > 1 ? armor[1] : null;
        ItemStack boots = armor != null && armor.length > 0 ? armor[0] : null;

        gui.setItem(HELMET_SLOT, buildInteractiveDisplayItem(helmet, Material.LEATHER_HELMET, ChatColor.GOLD + "Helmet", "Empty"));
        gui.setItem(CHESTPLATE_SLOT, buildInteractiveDisplayItem(chestplate, Material.LEATHER_CHESTPLATE, ChatColor.GOLD + "Chestplate", "Empty"));
        gui.setItem(LEGGINGS_SLOT, buildInteractiveDisplayItem(leggings, Material.LEATHER_LEGGINGS, ChatColor.GOLD + "Leggings", "Empty"));
        gui.setItem(BOOTS_SLOT, buildInteractiveDisplayItem(boots, Material.LEATHER_BOOTS, ChatColor.GOLD + "Boots", "Empty"));
        gui.setItem(OFFHAND_SLOT, buildInteractiveDisplayItem(offhand, Material.SHIELD, ChatColor.LIGHT_PURPLE + "Offhand", "Empty"));

        loadCraftingDisplayIntoGui(gui, craftingMatrix, craftingResult, cursorItem, targetOnline);
    }

    private void loadCraftingDisplayIntoGui(Inventory gui, ItemStack[] craftingMatrix, ItemStack craftingResult, ItemStack cursorItem, boolean targetOnline) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int fillerSlot : FILLER_SLOTS) {
            gui.setItem(fillerSlot, filler);
        }

        if (!targetOnline) {
            gui.setItem(CURSOR_SLOT, buildBarrier("Not Available", "Player is offline"));
            gui.setItem(RESULT_SLOT, buildBarrier("Not Available", "Player is offline"));
            for (int matrixSlot : MATRIX_SLOTS) {
                gui.setItem(matrixSlot, buildBarrier("Not Available", "Player is offline"));
            }
            return;
        }

        gui.setItem(CURSOR_SLOT, buildInteractiveDisplayItem(cursorItem, Material.TARGET, ChatColor.AQUA + "Currently Hovering", "Not holding anything"));
        gui.setItem(RESULT_SLOT, buildDisplayItem(craftingResult, ChatColor.GREEN + "Crafting Result", Material.CRAFTING_TABLE, "No result"));

        for (int i = 0; i < MATRIX_SLOT_COUNT; i++) {
            ItemStack matrixItem = craftingMatrix != null && i < craftingMatrix.length ? craftingMatrix[i] : null;
            gui.setItem(MATRIX_SLOTS[i], buildInteractiveDisplayItem(matrixItem, Material.LIGHT_GRAY_STAINED_GLASS_PANE, ChatColor.YELLOW + "Crafting Slot", "Empty"));
        }
    }

    private ItemStack buildBarrier(String label, String description) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + label);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + description));
            meta.getPersistentDataContainer().set(placeholderKey, PersistentDataType.BYTE, (byte) 1);
            barrier.setItemMeta(meta);
        }
        return barrier;
    }

    private ItemStack buildDisplayItem(ItemStack source, String label, Material placeholderMaterial, String placeholderText) {
        if (source != null && source.getType() != Material.AIR) {
            ItemStack display = source.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(label);
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "View only"));
                display.setItemMeta(meta);
            }
            return display;
        }

        return buildPlaceholder(placeholderMaterial, label, placeholderText);
    }

    private ItemStack buildInteractiveDisplayItem(ItemStack source, Material placeholderMaterial, String placeholderLabel, String placeholderText) {
        if (source != null && source.getType() != Material.AIR) {
            return source.clone();
        }

        return buildPlaceholder(placeholderMaterial, placeholderLabel, placeholderText);
    }

    private ItemStack buildPlaceholder(Material material, String label, String description) {
        ItemStack placeholder = new ItemStack(material);
        ItemMeta meta = placeholder.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(label);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + description));
            meta.getPersistentDataContainer().set(placeholderKey, PersistentDataType.BYTE, (byte) 1);
            placeholder.setItemMeta(meta);
        }
        return placeholder;
    }

    private boolean isPlaceholder(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(placeholderKey, PersistentDataType.BYTE);
    }

    private ItemStack extractEditableItem(Inventory gui, int slot) {
        ItemStack item = gui.getItem(slot);
        if (item == null || item.getType() == Material.AIR || isPlaceholder(item)) return null;
        return item.clone();
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        InventorySession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(session.inventory)) {
            int rawSlot = event.getRawSlot();
            if (!isEditableSlot(rawSlot, isTargetOnline(session))) {
                event.setCancelled(true);
                return;
            }

            if (isPlaceholder(event.getCurrentItem())) {
                event.setCancelled(true);
                handlePlaceholderClick(event, session, rawSlot);
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

    private void handlePlaceholderClick(InventoryClickEvent event, InventorySession session, int rawSlot) {
        ItemStack cursorItem = event.getCursor();
        if (cursorItem == null || cursorItem.getType() == Material.AIR) return;

        int amountToPlace = event.isRightClick() ? 1 : cursorItem.getAmount();
        ItemStack placed = cursorItem.clone();
        placed.setAmount(amountToPlace);
        session.inventory.setItem(rawSlot, placed);

        int remainingAmount = cursorItem.getAmount() - amountToPlace;
        if (remainingAmount <= 0) {
            event.getWhoClicked().setItemOnCursor(null);
        } else {
            ItemStack leftover = cursorItem.clone();
            leftover.setAmount(remainingAmount);
            event.getWhoClicked().setItemOnCursor(leftover);
        }
    }

    private void moveShiftClickedItemIntoSession(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Inventory topInventory = event.getView().getTopInventory();
        ItemStack remaining = clicked.clone();

        Integer armorSlot = getArmorSlotFor(remaining.getType());
        if (armorSlot != null) {
            remaining = placeIntoSingleSlot(topInventory, armorSlot, remaining);
        } else {
            remaining = fillSlots(topInventory, HOTBAR_SLOTS, remaining);
            remaining = fillSlots(topInventory, MAIN_INVENTORY_SLOTS, remaining);
        }

        event.setCurrentItem(remaining.getAmount() > 0 ? remaining : null);
    }

    private Integer getArmorSlotFor(Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET")) return HELMET_SLOT;
        if (name.endsWith("_CHESTPLATE") || material == Material.ELYTRA) return CHESTPLATE_SLOT;
        if (name.endsWith("_LEGGINGS")) return LEGGINGS_SLOT;
        if (name.endsWith("_BOOTS")) return BOOTS_SLOT;
        return null;
    }

    private ItemStack placeIntoSingleSlot(Inventory inventory, int slot, ItemStack remaining) {
        ItemStack existing = inventory.getItem(slot);
        if (existing == null || existing.getType() == Material.AIR || isPlaceholder(existing)) {
            int moved = Math.min(remaining.getMaxStackSize(), remaining.getAmount());
            ItemStack placed = remaining.clone();
            placed.setAmount(moved);
            inventory.setItem(slot, placed);
            remaining.setAmount(remaining.getAmount() - moved);
            return remaining;
        }

        if (existing.isSimilar(remaining)) {
            int space = existing.getMaxStackSize() - existing.getAmount();
            int moved = Math.min(space, remaining.getAmount());
            if (moved > 0) {
                existing.setAmount(existing.getAmount() + moved);
                remaining.setAmount(remaining.getAmount() - moved);
            }
        }

        return remaining;
    }

    private ItemStack fillSlots(Inventory inventory, int[] slots, ItemStack remaining) {
        for (int slot : slots) {
            if (remaining.getAmount() <= 0) break;
            ItemStack existing = inventory.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR || isPlaceholder(existing) || !existing.isSimilar(remaining)) continue;

            int space = existing.getMaxStackSize() - existing.getAmount();
            if (space <= 0) continue;

            int moved = Math.min(space, remaining.getAmount());
            existing.setAmount(existing.getAmount() + moved);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        for (int slot : slots) {
            if (remaining.getAmount() <= 0) break;
            ItemStack existing = inventory.getItem(slot);
            if (existing != null && existing.getType() != Material.AIR && !isPlaceholder(existing)) continue;

            int moved = Math.min(remaining.getMaxStackSize(), remaining.getAmount());
            ItemStack placed = remaining.clone();
            placed.setAmount(moved);
            inventory.setItem(slot, placed);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        return remaining;
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        InventorySession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        boolean targetOnline = isTargetOnline(session);
        boolean touchesEditableSlot = false;
        for (int slot : event.getRawSlots()) {
            if (slot >= GUI_SIZE) continue;

            if (!isEditableSlot(slot, targetOnline) || isPlaceholder(session.inventory.getItem(slot))) {
                event.setCancelled(true);
                return;
            }

            touchesEditableSlot = true;
        }

        if (touchesEditableSlot) {
            session.markLocalEdit();
            persistAndRefreshAfterInventoryEvent(session.viewerUUID);
        }
    }

    private boolean isEditableSlot(int rawSlot, boolean targetOnline) {
        if (rawSlot >= 0 && rawSlot <= 40) return true;
        if (!targetOnline) return false;
        if (rawSlot == CURSOR_SLOT) return true;
        for (int matrixSlot : MATRIX_SLOTS) {
            if (matrixSlot == rawSlot) return true;
        }
        return false;
    }

    private boolean isTargetOnline(InventorySession session) {
        return Bukkit.getPlayer(session.targetUUID) != null;
    }

    private void persistAndRefreshAfterInventoryEvent(UUID viewerUUID) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            InventorySession session = sessionsByViewer.get(viewerUUID);
            if (session == null) return;

            try {
                InventoryData data = persistSession(session);
                refreshOtherViewers(session, data);

                if (Bukkit.getPlayer(session.targetUUID) == null) {
                    loadDataIntoGui(session.inventory, data.contents, data.armor, data.offhand, false);
                }
            } catch (Exception e) {
                Player viewer = Bukkit.getPlayer(viewerUUID);
                if (viewer != null) {
                    viewer.sendMessage(ChatColor.RED + "Error saving inventory data!");
                }
                e.printStackTrace();
            }
        });
    }

    private InventoryData persistSession(InventorySession session) {
        InventoryData data = extractInventoryData(session.inventory);
        Player target = Bukkit.getPlayer(session.targetUUID);

        if (target != null) {
            target.getInventory().setContents(copyContents(data.contents, 36));
            target.getInventory().setArmorContents(copyContents(data.armor, 4));
            target.getInventory().setItemInOffHand(copyItem(data.offhand));
            target.updateInventory();
            syncCraftingDisplayToTarget(session, target);
        }

        saveOfflineInventory(session.targetUUID, data);
        return data;
    }

    private void syncCraftingDisplayToTarget(InventorySession session, Player target) {
        InventoryView openView = target.getOpenInventory();
        if (openView != null && openView.getTopInventory() instanceof CraftingInventory craftingInventory) {
            int actualMatrixSize = craftingInventory.getMatrix().length;
            ItemStack[] matrix = new ItemStack[actualMatrixSize];
            for (int i = 0; i < actualMatrixSize && i < MATRIX_SLOT_COUNT; i++) {
                matrix[i] = extractEditableItem(session.inventory, MATRIX_SLOTS[i]);
            }
            craftingInventory.setMatrix(matrix);
        }

        target.setItemOnCursor(extractEditableItem(session.inventory, CURSOR_SLOT));
    }

    private void saveOfflineInventory(UUID targetUUID, InventoryData data) {
        pendingSaves.merge(targetUUID, CompletableFuture.completedFuture(null),
                (prev, ignored) -> prev.thenRunAsync(() -> {
                    try {
                        OfflinePlayerDataManager.saveInventoryToFile(targetUUID, data.contents);
                        OfflinePlayerDataManager.saveArmorToFile(targetUUID, data.armor);
                        OfflinePlayerDataManager.saveOffhandToFile(targetUUID, data.offhand);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, asyncExecutor));
    }

    private void refreshOtherViewers(InventorySession changedSession, InventoryData data) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(changedSession.targetUUID);
        if (viewerUUIDs == null) return;

        boolean targetOnline = Bukkit.getPlayer(changedSession.targetUUID) != null;
        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            if (viewerUUID.equals(changedSession.viewerUUID)) continue;

            InventorySession otherSession = sessionsByViewer.get(viewerUUID);
            if (otherSession != null && !otherSession.isWaitingForLocalEdit()) {
                loadDataIntoGui(otherSession.inventory, data.contents, data.armor, data.offhand, targetOnline);
            }
        }
    }

    public void handleInventoryClose(InventoryCloseEvent event) {
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
                        loadDataIntoGui(session.inventory, data.contents, data.armor, data.offhand, false);
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
        armor[3] = extractEditableItem(gui, HELMET_SLOT);
        armor[2] = extractEditableItem(gui, CHESTPLATE_SLOT);
        armor[1] = extractEditableItem(gui, LEGGINGS_SLOT);
        armor[0] = extractEditableItem(gui, BOOTS_SLOT);

        return new InventoryData(contents, armor, extractEditableItem(gui, OFFHAND_SLOT));
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
