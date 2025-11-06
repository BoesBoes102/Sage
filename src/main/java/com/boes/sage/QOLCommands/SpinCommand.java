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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CommandAlias("spin")
@Description("Spin in circles")
public class SpinCommand extends BaseCommand implements Listener {

    private final Sage plugin;
    private final Set<UUID> spinning = new HashSet<>();
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public SpinCommand(Sage plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Conditions("permission:sage.spin")
    public void onCommand(Player player) {
        UUID uuid = player.getUniqueId();
        if (spinning.contains(uuid)) {
            stopSpin(player);
            player.sendMessage("§aStopped spinning!");
        } else {
            startSpin(player);
            player.sendMessage("§aStarted spinning!");
        }
    }

    private void startSpin(Player player) {
        UUID uuid = player.getUniqueId();
        spinning.add(uuid);

        BukkitRunnable task = new BukkitRunnable() {
            private float yaw = player.getLocation().getYaw();

            @Override
            public void run() {
                if (!player.isOnline() || !spinning.contains(uuid)) {
                    cancel();
                    return;
                }

                yaw += 10;
                if (yaw >= 360) yaw -= 360;

                try {
                    PacketContainer lookPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK);
                    lookPacket.getIntegers().write(0, player.getEntityId());
                    lookPacket.getBytes().write(0, (byte) ((yaw * 256.0F) / 360.0F));
                    lookPacket.getBytes().write(1, (byte) ((player.getLocation().getPitch() * 256.0F) / 360.0F));
                    protocolManager.broadcastServerPacket(lookPacket);

                    PacketContainer headPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
                    headPacket.getIntegers().write(0, player.getEntityId());
                    headPacket.getBytes().write(0, (byte) ((yaw * 256.0F) / 360.0F));
                    protocolManager.broadcastServerPacket(headPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 1L);
        tasks.put(uuid, task);
    }

    private void stopSpin(Player player) {
        UUID uuid = player.getUniqueId();
        spinning.remove(uuid);
        BukkitRunnable task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (spinning.contains(uuid) && event.isSneaking()) {
            stopSpin(player);
            player.sendMessage("§aStopped spinning!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (spinning.contains(player.getUniqueId())) {
            stopSpin(player);
        }
    }
}