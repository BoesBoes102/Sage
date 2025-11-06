package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@CommandAlias("openinv")
@Description("Open another player's inventory")
public class OpenInventoryCommand extends BaseCommand implements Listener {
    private final Sage plugin;
    private final Map<UUID, UUID> viewerToTarget = new HashMap<>();
    private final Map<UUID, Integer> updateTasks = new HashMap<>();
    private final Map<UUID, String> openOfflineInventories = new HashMap<>();

    public OpenInventoryCommand(Sage plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.openinv")
    public void onCommand(Player player, Player target) {
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot open your own inventory!");
            return;
        }

        try {
            openEnhancedInventory(player, target);
            player.sendMessage("§aOpened inventory of §e" + target.getName());
        } catch (Exception e) {
            player.sendMessage("§cError opening inventory!");
            e.printStackTrace();
        }
    }

    private void openEnhancedInventory(Player viewer, Player target) {
        String inventoryTitle = "§8" + target.getName() + "'s Inventory";
        Inventory gui = Bukkit.createInventory(null, 54, inventoryTitle);
        
        viewerToTarget.put(viewer.getUniqueId(), target.getUniqueId());
        
        updateInventoryView(gui, target, viewer);
        viewer.openInventory(gui);
        
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (viewer.isOnline() && target.isOnline()) {
                org.bukkit.inventory.InventoryView openInv = viewer.getOpenInventory();
                if (openInv != null && openInv.getTitle().equals(inventoryTitle)) {
                    updateInventoryView(gui, target, viewer);
                }
            }
        }, 1L, 1L).getTaskId();
        
        updateTasks.put(viewer.getUniqueId(), taskId);
    }

    private void updateInventoryView(Inventory gui, Player target, Player viewer) {
        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        sepMeta.setDisplayName("§7");
        separator.setItemMeta(sepMeta);
        
        ItemStack[] armor = target.getInventory().getArmorContents();
        gui.setItem(0, armor[3]);
        gui.setItem(1, armor[2]);
        gui.setItem(2, armor[1]);
        gui.setItem(3, armor[0]);
        gui.setItem(4, target.getInventory().getItemInOffHand());
        gui.setItem(5, separator);
        gui.setItem(6, separator);
        gui.setItem(7, separator);
        gui.setItem(8, separator);
        
        for (int i = 9; i < 27; i++) {
            gui.setItem(i, separator);
        }
        
        ItemStack[] targetContents = target.getInventory().getContents();
        for (int i = 9; i < 36 && i < targetContents.length; i++) {
            gui.setItem(27 + (i - 9), targetContents[i]);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) return;

        UUID viewerUUID = viewer.getUniqueId();
        
        if (viewerToTarget.containsKey(viewerUUID)) {
            handleOnlineInventoryClick(event, viewer);
        }
    }
    
    private void handleOnlineInventoryClick(InventoryClickEvent event, Player viewer) {
        UUID targetUUID = viewerToTarget.get(viewer.getUniqueId());
        Player target = Bukkit.getPlayer(targetUUID);
        
        if (target == null) {
            event.setCancelled(true);
            viewer.closeInventory();
            viewer.sendMessage("§cTarget player is no longer online!");
            return;
        }
        
        String inventoryTitle = "§8" + target.getName() + "'s Inventory";
        
        if (event.getClickedInventory() != null && event.getView().getTitle().equals(inventoryTitle)) {
            int slot = event.getRawSlot();
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            
            if (slot >= 5 && slot <= 26) {
                return;
            }
            
            if (slot >= 0 && slot <= 3) {
                handleArmorSlot(slot, clicked, cursor, target, viewer);
                updateInventoryView(event.getClickedInventory(), target, viewer);
                return;
            }
            
            if (slot == 4) {
                handleOffhandSlot(clicked, cursor, target, viewer);
                updateInventoryView(event.getClickedInventory(), target, viewer);
                return;
            }
            
            if (slot >= 27 && slot < 54) {
                handleInventorySlot(slot - 27 + 9, clicked, cursor, target, viewer, event.getClick());
                updateInventoryView(event.getClickedInventory(), target, viewer);
            }
        } 
        else if (event.getClickedInventory() == viewer.getInventory() && event.getView().getTitle().equals(inventoryTitle)) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            
            if (slot >= 0 && slot < viewer.getInventory().getSize()) {
                handleViewerInventoryTransfer(slot, clicked, cursor, target, viewer, event.getClick());
                updateInventoryView(event.getView().getTopInventory(), target, viewer);
            }
        }
    }
    
    private void handleViewerInventoryTransfer(int slot, ItemStack clicked, ItemStack cursor, Player target, Player viewer, org.bukkit.event.inventory.ClickType clickType) {
        switch (clickType) {
            case LEFT:
                if (clicked != null && clicked.getType() != Material.AIR) {
                    target.getInventory().addItem(clicked);
                    viewer.getInventory().setItem(slot, cursor);
                }
                break;
            case RIGHT:
                if (clicked != null && clicked.getType() != Material.AIR) {
                    ItemStack single = clicked.clone();
                    single.setAmount(1);
                    target.getInventory().addItem(single);
                    clicked.setAmount(clicked.getAmount() - 1);
                    viewer.getInventory().setItem(slot, clicked.getAmount() > 0 ? clicked : cursor);
                }
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                if (clicked != null && clicked.getType() != Material.AIR) {
                    target.getInventory().addItem(clicked);
                    viewer.getInventory().setItem(slot, null);
                }
                break;
        }
    }
    
    private void handleArmorSlot(int slot, ItemStack clicked, ItemStack cursor, Player target, Player viewer) {
        ItemStack[] armor = target.getInventory().getArmorContents();
        int armorIndex = 3 - slot;
        
        armor[armorIndex] = cursor;
        target.getInventory().setArmorContents(armor);
        viewer.setItemOnCursor(clicked);
    }
    
    private void handleOffhandSlot(ItemStack clicked, ItemStack cursor, Player target, Player viewer) {
        target.getInventory().setItemInOffHand(cursor);
        viewer.setItemOnCursor(clicked);
    }
    
    private void handleInventorySlot(int targetSlot, ItemStack clicked, ItemStack cursor, Player target, Player viewer, org.bukkit.event.inventory.ClickType clickType) {
        switch (clickType) {
            case LEFT:
                target.getInventory().setItem(targetSlot, cursor);
                viewer.setItemOnCursor(clicked);
                break;
            case RIGHT:
                if (cursor != null && cursor.getType() != Material.AIR) {
                    ItemStack single = cursor.clone();
                    single.setAmount(1);
                    target.getInventory().setItem(targetSlot, single);
                    cursor.setAmount(cursor.getAmount() - 1);
                    viewer.setItemOnCursor(cursor.getAmount() > 0 ? cursor : null);
                } else if (clicked != null && clicked.getType() != Material.AIR) {
                    int half = (clicked.getAmount() + 1) / 2;
                    ItemStack halfStack = clicked.clone();
                    halfStack.setAmount(half);
                    viewer.setItemOnCursor(halfStack);
                    clicked.setAmount(clicked.getAmount() - half);
                    target.getInventory().setItem(targetSlot, clicked.getAmount() > 0 ? clicked : null);
                }
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                if (clicked != null && clicked.getType() != Material.AIR) {
                    viewer.getInventory().addItem(clicked);
                    target.getInventory().setItem(targetSlot, null);
                }
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) return;

        UUID viewerUUID = viewer.getUniqueId();
        
        if (viewerToTarget.containsKey(viewerUUID)) {
            viewerToTarget.remove(viewerUUID);
            
            if (updateTasks.containsKey(viewerUUID)) {
                Bukkit.getScheduler().cancelTask(updateTasks.get(viewerUUID));
                updateTasks.remove(viewerUUID);
            }
        }
    }

    public void cleanup() {
        for (Integer taskId : new ArrayList<>(updateTasks.values())) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        updateTasks.clear();
        viewerToTarget.clear();
    }
}