package com.boes.sage.features.alts;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AltAccountService {
    private final Sage plugin;
    private final DatabaseManager databaseManager;

    public AltAccountService(Sage plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void trackLogin(Player player) {
        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        String name = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> trackLoginBlocking(uuid, name, ip));
    }

    private synchronized void trackLoginBlocking(UUID uuid, String name, String ip) {
        try (Connection connection = databaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO alt_players (uuid, last_known_name, last_ip) VALUES (?, ?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET last_known_name = excluded.last_known_name, last_ip = excluded.last_ip")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, name);
                statement.setString(3, ip);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO alt_player_ips (uuid, ip) VALUES (?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, ip);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to track login for " + uuid + ": " + e.getMessage());
        }
    }

    public synchronized AltPlayerRecord getPlayerRecord(UUID uuid) {
        try (Connection connection = databaseManager.getConnection()) {
            String lastKnownName = null;
            String lastIp = null;

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT last_known_name, last_ip FROM alt_players WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    lastKnownName = resultSet.getString("last_known_name");
                    lastIp = resultSet.getString("last_ip");
                }
            }

            Set<String> ips = new LinkedHashSet<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT ip FROM alt_player_ips WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ips.add(resultSet.getString("ip"));
                    }
                }
            }

            return new AltPlayerRecord(uuid, resolveName(uuid, lastKnownName), lastIp, ips);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load player record for " + uuid, e);
        }
    }

    public synchronized List<AltMatch> findMatchingLatestIp(UUID targetUuid) {
        AltPlayerRecord targetRecord = getPlayerRecord(targetUuid);
        if (targetRecord == null || targetRecord.lastIp() == null || targetRecord.lastIp().isEmpty()) {
            return List.of();
        }

        return buildMatchesForIps(targetUuid, List.of(targetRecord.lastIp())).getOrDefault(targetRecord.lastIp(), List.of());
    }

    public synchronized Map<String, List<AltMatch>> findMatchingAllIps(UUID targetUuid) {
        AltPlayerRecord targetRecord = getPlayerRecord(targetUuid);
        if (targetRecord == null || targetRecord.ips().isEmpty()) {
            return Map.of();
        }

        return buildMatchesForIps(targetUuid, targetRecord.ips());
    }

    private Map<String, List<AltMatch>> buildMatchesForIps(UUID targetUuid, Collection<String> ipsToCheck) {
        Map<String, List<UUID>> uuidsByIp = new LinkedHashMap<>();
        Set<UUID> allLinkedUuids = new LinkedHashSet<>();

        try (Connection connection = databaseManager.getConnection()) {
            for (String ip : ipsToCheck) {
                List<UUID> linkedUuids = new ArrayList<>();
                Set<UUID> seen = new LinkedHashSet<>();

                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT uuid FROM alt_player_ips WHERE ip = ?")) {
                    statement.setString(1, ip);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            UUID linkedUuid;
                            try {
                                linkedUuid = UUID.fromString(resultSet.getString("uuid"));
                            } catch (IllegalArgumentException ignored) {
                                continue;
                            }

                            if (linkedUuid.equals(targetUuid) || !seen.add(linkedUuid)) {
                                continue;
                            }

                            linkedUuids.add(linkedUuid);
                            allLinkedUuids.add(linkedUuid);
                        }
                    }
                }

                if (!linkedUuids.isEmpty()) {
                    uuidsByIp.put(ip, linkedUuids);
                }
            }

            Map<UUID, String> lastKnownNames = fetchLastKnownNames(connection, allLinkedUuids);

            Map<String, List<AltMatch>> matchesByIp = new LinkedHashMap<>();
            for (Map.Entry<String, List<UUID>> entry : uuidsByIp.entrySet()) {
                List<AltMatch> matches = new ArrayList<>();
                for (UUID linkedUuid : entry.getValue()) {
                    String name = resolveName(linkedUuid, lastKnownNames.get(linkedUuid));
                    boolean online = Bukkit.getPlayer(linkedUuid) != null;
                    matches.add(new AltMatch(linkedUuid, name, online));
                }
                matchesByIp.put(entry.getKey(), matches);
            }

            return matchesByIp;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to build alt matches", e);
        }
    }

    private Map<UUID, String> fetchLastKnownNames(Connection connection, Set<UUID> uuids) throws SQLException {
        Map<UUID, String> names = new HashMap<>();
        if (uuids.isEmpty()) {
            return names;
        }

        String placeholders = String.join(",", uuids.stream().map(u -> "?").toArray(String[]::new));
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid, last_known_name FROM alt_players WHERE uuid IN (" + placeholders + ")")) {
            int index = 1;
            for (UUID uuid : uuids) {
                statement.setString(index++, uuid.toString());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    try {
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        names.put(uuid, resultSet.getString("last_known_name"));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        return names;
    }

    private String resolveName(UUID uuid, String fallback) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getName() != null) {
            return offlinePlayer.getName();
        }

        return fallback != null ? fallback : uuid.toString();
    }

    public record AltPlayerRecord(UUID uuid, String name, String lastIp, Set<String> ips) {
    }

    public record AltMatch(UUID uuid, String name, boolean online) {
    }
}
