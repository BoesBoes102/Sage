package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CommandAlias("crawl")
@Description("Crawl on the ground")
public class CrawlCommand extends BaseCommand implements Listener {

    private final Sage plugin;
    private final ProtocolManager protocolManager;
    private final Set<UUID> crawling = new HashSet<>();
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();

    public CrawlCommand(Sage plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Conditions("permission:sage.crawl")
    public void onCommand(Player player) {
        UUID uuid = player.getUniqueId();

        if (crawling.contains(uuid)) {
            stopCrawling(player);
            player.sendMessage("§aYou stopped crawling!");
            return;
        }

        startCrawling(player);
        player.sendMessage("§aYou started crawling!");
    }

    private void startCrawling(Player player) {
        UUID uuid = player.getUniqueId();
        crawling.add(uuid);
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);

        setPlayerPose(player, 2);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !crawling.contains(uuid)) {
                    cancel();
                    return;
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 5L);
        tasks.put(uuid, task);
    }

    private void stopCrawling(Player player) {
        UUID uuid = player.getUniqueId();
        crawling.remove(uuid);
        BukkitRunnable task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        setPlayerPose(player, 0);
    }

    private void setPlayerPose(Player player, int poseId) {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, player.getEntityId());

            List<WrappedDataValue> dataValues = new ArrayList<>();
            dataValues.add(new WrappedDataValue(6, getByteSerializer(), (byte) poseId));

            packet.getDataValueCollectionModifier().write(0, dataValues);

            for (Player online : Bukkit.getOnlinePlayers()) {
                try {
                    protocolManager.sendServerPacket(online, packet);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send crawl pose packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer getByteSerializer() {
        return com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry.get(Byte.class);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (crawling.contains(uuid) && event.isSneaking()) {
            stopCrawling(player);
            player.sendMessage("§aYou stopped crawling!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (crawling.contains(player.getUniqueId())) {
            stopCrawling(player);
        }
    }
}