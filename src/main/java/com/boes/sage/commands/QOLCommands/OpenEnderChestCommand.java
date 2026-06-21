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

@CommandAlias("openender|endersee|ec|viewender")
@Description("Open another player's ender chest")
@CommandPermission("sage.openender")
public class OpenEnderChestCommand extends BaseCommand implements Listener {
    private static final int ENDER_CHEST_SIZE = 27;
    private static final long LOCAL_EDIT_REFRESH_DELAY_MS = 300L;

    private final Sage plugin;
    private final Map<UUID, EnderChestSession> sessionsByViewer = new HashMap<>();
    private final Map<UUID, Set<UUID>> viewersByTarget = new HashMap<>();

    public OpenEnderChestCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onCommand(Player viewer, String[] args) {
        if (args.length < 1) {
            viewer.sendMessage(ChatColor.RED + "Usage: /openender <player>");
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
            viewer.sendMessage(ChatColor.RED + "You cannot open your own ender chest!");
            return;
        }

        try {
            openEnderChest(viewer, targetUUID, targetName);
            viewer.sendMessage(ChatColor.GREEN + "Opened ender chest of " + ChatColor.YELLOW + targetName);
        } catch (Exception e) {
            viewer.sendMessage(ChatColor.RED + "Error opening ender chest!");
            e.printStackTrace();
        }
    }

    private void openEnderChest(Player viewer, UUID targetUUID, String targetName) throws Exception {
        UUID viewerUUID = viewer.getUniqueId();
        endSession(viewerUUID, true);

        Inventory inventory = Bukkit.createInventory(null, ENDER_CHEST_SIZE, getTitle(targetName));
        EnderChestSession session = new EnderChestSession(viewerUUID, targetUUID, inventory);
        loadSourceIntoSession(session, true);

        viewer.openInventory(inventory);
        sessionsByViewer.put(viewerUUID, session);
        viewersByTarget.computeIfAbsent(targetUUID, ignored -> new HashSet<>()).add(viewerUUID);
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

    private void loadSourceIntoSession(EnderChestSession session, boolean preferOnline) throws Exception {
        Player target = preferOnline ? Bukkit.getPlayer(session.targetUUID) : null;
        if (target != null) {
            session.inventory.setContents(copyContents(target.getEnderChest().getContents()));
            return;
        }

        session.inventory.setContents(copyContents(OfflinePlayerDataManager.loadOfflineEnderChest(session.targetUUID)));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        EnderChestSession session = sessionsByViewer.get(viewer.getUniqueId());
        if (session == null || !event.getView().getTopInventory().equals(session.inventory)) return;

        session.markLocalEdit();
        persistAndRefreshAfterInventoryEvent(session.viewerUUID);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
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

    private void persistSession(EnderChestSession session) throws Exception {
        ItemStack[] contents = copyContents(session.inventory.getContents());
        Player target = Bukkit.getPlayer(session.targetUUID);

        if (target != null) {
            target.getEnderChest().setContents(contents);
        }

        OfflinePlayerDataManager.saveOfflineEnderChest(session.targetUUID, contents);
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;
        endSession(viewer.getUniqueId(), true);
    }

    public void prepareForPlayerJoin(Player player) {
        Set<UUID> viewerUUIDs = viewersByTarget.get(player.getUniqueId());
        if (viewerUUIDs == null) return;

        for (UUID viewerUUID : new ArrayList<>(viewerUUIDs)) {
            EnderChestSession session = sessionsByViewer.get(viewerUUID);
            if (session == null) continue;

            try {
                OfflinePlayerDataManager.saveOfflineEnderChest(session.targetUUID, session.inventory.getContents());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handlePlayerQuit(Player player) {
        UUID playerUUID = player.getUniqueId();
        endSession(playerUUID, true);

        try {
            ItemStack[] contents = getMostRecentTargetContents(player);
            OfflinePlayerDataManager.saveOfflineEnderChest(playerUUID, contents);

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

            try {
                if (Bukkit.getPlayer(playerUUID) == null) {
                    OfflinePlayerDataManager.saveOfflineEnderChest(session.targetUUID, session.inventory.getContents());
                } else {
                    persistSession(session);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
