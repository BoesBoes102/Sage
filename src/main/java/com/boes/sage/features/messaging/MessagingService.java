package com.boes.sage.features.messaging;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MessagingService {
    private final Sage plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, UUID> lastMessageSender = new HashMap<>();
    private final Set<UUID> messageSpies;

    public MessagingService(Sage plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.messageSpies = new HashSet<>();
        loadData();
    }

    private void loadData() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM messaging_spies");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    messageSpies.add(UUID.fromString(resultSet.getString("uuid")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load messaging data", e);
        }
    }

    public void sendPrivateMessage(Player sender, String targetName, String message) {
        Player target = resolveTarget(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "You cannot message yourself!");
            return;
        }

        deliver(sender, target, message);
    }

    public void replyToLastMessage(Player sender, String message) {
        UUID targetUUID = lastMessageSender.get(sender.getUniqueId());
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "You have no one to reply to!");
            return;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "That player is no longer online!");
            return;
        }

        deliver(sender, target, message);
    }

    private void deliver(Player sender, Player target, String message) {
        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "me" + ChatColor.GRAY + " -> "
            + ChatColor.RED + target.getName() + ChatColor.GRAY + "] " + ChatColor.WHITE + message);
        target.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + sender.getName() + ChatColor.GRAY + " -> "
            + ChatColor.RED + "me" + ChatColor.GRAY + "] " + ChatColor.WHITE + message);

        lastMessageSender.put(target.getUniqueId(), sender.getUniqueId());

        broadcastToSpies(sender, target, message);
    }

    private void broadcastToSpies(Player sender, Player target, String message) {
        if (messageSpies.isEmpty()) return;

        String spyLine = ChatColor.DARK_GRAY + "[MsgSpy] " + ChatColor.YELLOW + sender.getName()
            + ChatColor.GRAY + " -> " + ChatColor.YELLOW + target.getName()
            + ChatColor.GRAY + ": " + ChatColor.WHITE + message;

        for (UUID spyUUID : messageSpies) {
            Player spy = Bukkit.getPlayer(spyUUID);
            if (spy != null && spy.isOnline() && spy.hasPermission("sage.messagespy")) {
                spy.sendMessage(spyLine);
            }
        }
    }

    private Player resolveTarget(String name) {
        Player exact = Bukkit.getPlayerExact(name);
        return exact != null ? exact : Bukkit.getPlayer(name);
    }

    public boolean hasMessageSpy(Player player) {
        return messageSpies.contains(player.getUniqueId());
    }

    public void setMessageSpy(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (enabled) {
            messageSpies.add(uuid);
        } else {
            messageSpies.remove(uuid);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection()) {
                if (enabled) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT OR IGNORE INTO messaging_spies (uuid) VALUES (?)")) {
                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM messaging_spies WHERE uuid = ?")) {
                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save message spy state for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public boolean toggleMessageSpy(Player player) {
        boolean newState = !hasMessageSpy(player);
        setMessageSpy(player, newState);
        return newState;
    }
}
