package com.boes.sage.features.openinv;

import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class OpenEnderChestService {
    private static final int ENDER_CHEST_SIZE = 27;
    private static final long LOCAL_EDIT_REFRESH_DELAY_MS = 300L;

    private final Sage plugin;
    private final Map<UUID, EnderChestSession> sessionsByViewer = new HashMap<>();
    private final Map<UUID, Set<UUID>> viewersByTarget = new HashMap<>();
    private final Map<UUID, CompletableFuture<Void>> pendingSaves = new ConcurrentHashMap<>();
    private final Executor asyncExecutor;

    public OpenEnderChestService(Sage plugin) {
        this.plugin = plugin;
        this.asyncExecutor = runnable -> Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    public void openEnderChest(Player viewer, UUID targetUUID, String targetName) throws Exception {
        UUID viewerUUID = viewer.getUniqueId();
        endSession(viewerUUID, true);

        Inventory inventory = Bukkit.createInventory(null, ENDER_CHEST_SIZE, getTitle(targetName));
        EnderChestSession session = new EnderChestSession(viewerUUID, targetUUID, inventory);

        Player onlineTarget = Bukkit.getPlayer(targetUUID);
        if (onlineTarget != null) {
            inventory.setContents(copyContents(onlineTarget.getEnderChest().getContents()));
            finishOpeningEnderChest(viewer, session);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ItemStack[] contents = copyContents(OfflinePlayerDataManager.loadOfflineEnderChest(targetUUID));

                Bukkit.getScheduler().runTask(plugin, () -> {
                    inventory.setContents(contents);
                    finishOpeningEnderChest(viewer, session);
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (viewer.isOnline()) {
                        viewer.sendMessage(ChatColor.RED + "Error loading offline ender chest data!");
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    private void finishOpeningEnderChest(Player viewer, EnderChestSession session) {
        viewer.openInventory(session.inventory);
        sessionsByViewer.put(session.viewerUUID, session);
        viewersByTarget.computeIfAbsent(session.targetUUID, ignored -> new HashSet<>()).add(session.viewerUUID);
        startRefreshTask(session);
    }

    private void startRefreshTask(EnderChestSession session) {
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player viewer = Bukkit.getPlayer(session.viewerUUID);
            if (viewer == null || !viewer.isOnline()) {
                endSession(session.viewerUUID, true);
                return;
            }

            EnderChestSession currentSession = sessionsByViewer.get(session.viewerUUID);
            if (currentSession != session || !isViewingSession(viewer, session)) {
                endSession(session.viewerUUID, true);
                return;
            }

            Player target = Bukkit.getPlayer(session.targetUUID);
            if (target != null && !session.isWaitingForLocalEdit()) {
                session.inventory.setContents(copyContents(target.getEnderChest().getContents()));
            }
        }, 1L, 2L).getTaskId();

        session.taskId = taskId;
    }

    private boolean isViewingSession(Player viewer, EnderChestSession session) {
        return viewer.getOpenInventory() != null
                && viewer.getOpenInventory().getTopInventory().equals(session.inventory);
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        EnderChestSession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        session.markLocalEdit();
        persistAndRefreshAfterInventoryEvent(session.viewerUUID);
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        EnderChestSession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        for (int slot : event.getRawSlots()) {
            if (slot < session.inventory.getSize()) {
                session.markLocalEdit();
                persistAndRefreshAfterInventoryEvent(session.viewerUUID);
                return;
            }
        }
    }

    private void persistAndRefreshAfterInventoryEvent(UUID viewerUUID) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            EnderChestSession session = sessionsByViewer.get(viewerUUID);
            if (session == null) return;

            try {
                persistSession(session);
                refreshOtherViewers(session);
            } catch (Exception e) {
                Player viewer = Bukkit.getPlayer(viewerUUID);
                if (viewer != null) {
                    viewer.sendMessage(ChatColor.RED + "Error saving ender chest data!");
                }
                e.printStackTrace();
            }
        });
    }

    private void persistSession(EnderChestSession session) {
        ItemStack[] contents = copyContents(session.inventory.getContents());
        Player target = Bukkit.getPlayer(session.targetUUID);

        if (target != null) {
            target.getEnderChest().setContents(contents);
        }

        queueOfflineEnderChestSave(session.targetUUID, contents);
    }

    private void queueOfflineEnderChestSave(UUID targetUUID, ItemStack[] contents) {
        pendingSaves.merge(targetUUID, CompletableFuture.completedFuture(null),
                (prev, ignored) -> prev.thenRunAsync(() -> {
                    try {
                        OfflinePlayerDataManager.saveOfflineEnderChest(targetUUID, contents);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, asyncExecutor));
    }

    private void refreshOtherViewers(EnderChestSession changedSession) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(changedSession.targetUUID);
        if (viewerUUIDs == null) return;

        ItemStack[] contents = copyContents(changedSession.inventory.getContents());
        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            if (viewerUUID.equals(changedSession.viewerUUID)) continue;

            EnderChestSession otherSession = sessionsByViewer.get(viewerUUID);
            if (otherSession != null && !otherSession.isWaitingForLocalEdit()) {
                otherSession.inventory.setContents(copyContents(contents));
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
            EnderChestSession session = sessionsByViewer.get(viewerUUID);
            if (session == null) continue;

            queueOfflineEnderChestSave(session.targetUUID, copyContents(session.inventory.getContents()));
        }
    }

    public void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        endSession(playerUUID, true);

        try {
            ItemStack[] contents = getMostRecentTargetContents(player);
            queueOfflineEnderChestSave(playerUUID, contents);

            Set<UUID> viewerUUIDs = viewersByTarget.get(playerUUID);
            if (viewerUUIDs != null) {
                for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
                    EnderChestSession session = sessionsByViewer.get(viewerUUID);
                    if (session != null && !session.isWaitingForLocalEdit()) {
                        session.inventory.setContents(copyContents(contents));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemStack[] getMostRecentTargetContents(Player target) {
        ItemStack[] contents = copyContents(target.getEnderChest().getContents());
        Set<UUID> viewerUUIDs = viewersByTarget.get(target.getUniqueId());
        if (viewerUUIDs == null) {
            return contents;
        }

        EnderChestSession newestEditedSession = null;
        for (UUID viewerUUID : viewerUUIDs) {
            EnderChestSession session = sessionsByViewer.get(viewerUUID);
            if (session == null || session.lastLocalEditAt == 0L) continue;

            if (newestEditedSession == null || session.lastLocalEditAt > newestEditedSession.lastLocalEditAt) {
                newestEditedSession = session;
            }
        }

        return newestEditedSession != null ? copyContents(newestEditedSession.inventory.getContents()) : contents;
    }

    public void closeEnderChestsForPlayer(UUID playerUUID) {
        handleTargetTransition(playerUUID);
    }

    public void handleTargetTransition(UUID playerUUID) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(playerUUID);
        if (viewerUUIDs == null) return;

        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            EnderChestSession session = sessionsByViewer.get(viewerUUID);
            if (session == null) continue;

            if (Bukkit.getPlayer(playerUUID) == null) {
                queueOfflineEnderChestSave(session.targetUUID, copyContents(session.inventory.getContents()));
            } else {
                persistSession(session);
            }
        }
    }

    private void endSession(UUID viewerUUID, boolean save) {
        EnderChestSession session = sessionsByViewer.remove(viewerUUID);
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

    private ItemStack[] copyContents(ItemStack[] contents) {
        ItemStack[] copy = new ItemStack[ENDER_CHEST_SIZE];
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
        return ChatColor.DARK_GRAY + targetName + "'s Ender Chest";
    }

    private static class EnderChestSession {
        private final UUID viewerUUID;
        private final UUID targetUUID;
        private final Inventory inventory;
        private int taskId = -1;
        private long skipRefreshUntil = 0L;
        private long lastLocalEditAt = 0L;

        private EnderChestSession(UUID viewerUUID, UUID targetUUID, Inventory inventory) {
            this.viewerUUID = viewerUUID;
            this.targetUUID = targetUUID;
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
