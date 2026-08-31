package com.boes.sage.features.punishment;

import com.boes.sage.Sage;
import com.boes.sage.Utils.DatabaseManager;
import com.boes.sage.features.punishment.data.PunishmentHistory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PunishmentService {
    private static final String RED = "\u00A7c";
    private static final String YELLOW = "\u00A7e";
    private static final String DARK_RED = "\u00A74";
    private static final String GRAY = "\u00A77";
    private static final String WHITE = "\u00A7f";
    private static final String BOLD = "\u00A7l";
    private static final Title.Times SCREEN_TITLE_TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000));

    private final Sage plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final Map<String, Long> mutedIPs = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PunishmentService(Sage plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    private Component miniMessage(String template, String reason, String duration) {
        TagResolver placeholders = TagResolver.resolver(
            Placeholder.unparsed("reason", reason == null ? "" : reason),
            Placeholder.unparsed("duration", duration == null ? "permanent" : duration)
        );
        return miniMessage.deserialize(template, placeholders);
    }

    public int getPlayerStack(UUID uuid, String reason) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT stack FROM punishment_stacks WHERE uuid = ? AND reason = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, reason);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("stack") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load punishment stack for " + uuid, e);
        }
    }

    public void incrementStack(UUID uuid, String reason) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO punishment_stacks (uuid, reason, stack) VALUES (?, ?, 1) " +
                         "ON CONFLICT(uuid, reason) DO UPDATE SET stack = stack + 1")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, reason);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to increment punishment stack for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public void resetStack(UUID uuid, String reason) {
        setStack(uuid, reason, 0);
    }

    private void setStack(UUID uuid, String reason, int stack) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO punishment_stacks (uuid, reason, stack) VALUES (?, ?, ?) " +
                     "ON CONFLICT(uuid, reason) DO UPDATE SET stack = excluded.stack")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, reason);
            statement.setInt(3, stack);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save punishment stack for " + uuid, e);
        }
    }

    private void addHistory(UUID uuid, String type, String reason, String punisher, String duration) {
        long timestamp = System.currentTimeMillis();
        String storedDuration = duration == null ? "permanent" : duration;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO punishment_history (uuid, type, reason, punisher, timestamp, duration) VALUES (?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, type);
                statement.setString(3, reason);
                statement.setString(4, punisher);
                statement.setLong(5, timestamp);
                statement.setString(6, storedDuration);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to add punishment history for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public List<PunishmentHistory> getPlayerHistory(UUID uuid) {
        List<PunishmentHistory> result = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT type, reason, punisher, timestamp, duration FROM punishment_history WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    try {
                        String type = resultSet.getString("type");
                        String reason = resultSet.getString("reason");
                        String punisher = resultSet.getString("punisher");
                        long timestamp = resultSet.getLong("timestamp");
                        String duration = resultSet.getString("duration");

                        result.add(new PunishmentHistory(type, reason, punisher, timestamp, "permanent".equals(duration) ? null : duration, uuid));
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load punishment history for " + uuid, e);
        }

        result.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return result;
    }

    public List<PunishmentHistory> getHistoryByType(UUID uuid, String type) {
        return getPlayerHistory(uuid).stream()
                .filter(h -> h.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public boolean removeHistoryByTimestamp(UUID uuid, long timestamp) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM punishment_history WHERE uuid = ? AND timestamp = ?")) {
            statement.setString(1, uuid.toString());
            statement.setLong(2, timestamp);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove punishment history for " + uuid, e);
        }
    }

    public boolean removeHistoryByIndex(UUID uuid, String type, int index) {
        List<PunishmentHistory> history = getHistoryByType(uuid, type);

        if (index < 0 || index >= history.size()) {
            return false;
        }

        PunishmentHistory toRemove = history.get(index);
        return removeHistoryByTimestamp(uuid, toRemove.getTimestamp());
    }

    public void warn(OfflinePlayer target, String reason, CommandSender issuer) {
        plugin.getLogger().info(issuer.getName() + " warned " + target.getName() + " for " + reason);

        String warnTitle = plugin.getMessagesConfig().getString("messages.warn-title", "<yellow><bold>WARNED</bold></yellow>");
        String warnMessage = plugin.getMessagesConfig().getString("messages.warn-message", "<yellow><bold>You have been warned!</bold></yellow>\n<gray>Reason: <white><reason></white></gray>");

        if (target.isOnline()) {
            Component title = miniMessage(warnTitle, reason, null);
            Component subtitle = miniMessage("<gray><reason></gray>", reason, null);
            Objects.requireNonNull(target.getPlayer()).showTitle(Title.title(title, subtitle, SCREEN_TITLE_TIMES));
        } else {
            plugin.getNotificationService().addNotification(target.getUniqueId(), warnTitle, warnMessage, reason, "");
        }

        addHistory(target.getUniqueId(), "warn", reason, issuer.getName(), null);
        broadcastStaff(YELLOW + issuer.getName() + " " + GRAY + "warned " + YELLOW + target.getName() + " " + GRAY + "for " + WHITE + reason);
    }

    public boolean isMuted(UUID playerUUID) {
        Long muteExpiry = mutedPlayers.get(playerUUID);
        if (muteExpiry == null) {
            return false;
        }

        if (muteExpiry == -1) {
            return true;
        }

        if (System.currentTimeMillis() > muteExpiry) {
            mutedPlayers.remove(playerUUID);
            return false;
        }

        return true;
    }

    public boolean isMutedByIP(String ip) {
        Long muteExpiry = mutedIPs.get(ip);
        if (muteExpiry == null) {
            return false;
        }

        if (muteExpiry == -1) {
            return true;
        }

        if (System.currentTimeMillis() > muteExpiry) {
            mutedIPs.remove(ip);
            return false;
        }

        return true;
    }

    public boolean isAlreadyMuted(UUID playerUUID) {
        return isMuted(playerUUID);
    }

    public boolean isAlreadyBanned(OfflinePlayer target) {
        return Bukkit.getBanList(BanList.Type.NAME).isBanned(Objects.requireNonNull(target.getName()));
    }

    public boolean isAlreadyBlacklisted(OfflinePlayer target) {
        String reason = null;
        BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(Objects.requireNonNull(target.getName()));
        if (entry != null) {
            reason = entry.getReason();
        }
        return reason != null && reason.contains("[BLACKLISTED]");
    }

    public void mute(OfflinePlayer target, String reason, String duration, CommandSender issuer) {
        if (isAlreadyMuted(target.getUniqueId())) {
            issuer.sendMessage(RED + target.getName() + " is already muted!");
            return;
        }

        long muteExpiry = duration == null || duration.equalsIgnoreCase("permanent") ? -1 : System.currentTimeMillis() + parseDuration(duration);
        mutedPlayers.put(target.getUniqueId(), muteExpiry);

        String muteTitle = plugin.getMessagesConfig().getString("messages.mute-title", "<red><bold>MUTED</bold></red>");
        String muteMessage = plugin.getMessagesConfig().getString("messages.mute-message", "<red><bold>You have been muted!</bold></red>\n<gray>Reason: <white><reason></white></gray>\n<gray>Duration: <white><duration></white></gray>");

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress())).getAddress().getHostAddress();
            mutedIPs.put(ip, muteExpiry);

            Component title = miniMessage(muteTitle, reason, duration);
            Component subtitle = miniMessage("<gray>Reason: <white><reason></white> <dark_gray>|</dark_gray> Duration: <white><duration></white></gray>", reason, duration);
            Objects.requireNonNull(target.getPlayer()).showTitle(Title.title(title, subtitle, SCREEN_TITLE_TIMES));
        } else {
            plugin.getNotificationService().addNotification(target.getUniqueId(), muteTitle, muteMessage, reason, duration == null ? "permanent" : duration);
        }

        addHistory(target.getUniqueId(), "mute", reason, issuer.getName(), duration);
        broadcastStaff(YELLOW + issuer.getName() + " " + GRAY + "muted " + YELLOW + target.getName() + " " + GRAY + "for " + WHITE + duration + " " + GRAY + "(Reason: " + reason + ")");
    }

    public void ban(OfflinePlayer target, String reason, String duration, CommandSender issuer) {
        if (isAlreadyBanned(target)) {
            issuer.sendMessage(RED + target.getName() + " is already banned!");
            return;
        }

        Date expiry = duration == null ? null : new Date(System.currentTimeMillis() + parseDuration(duration));

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress()).getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, expiry, issuer.getName());
            storePlayerIP(target.getUniqueId(), ip);
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(target.getName()), reason, expiry, issuer.getName());

        if (target.isOnline()) {
            String banScreen = plugin.getMessagesConfig().getString("messages.ban-screen", "<red><bold>You have been banned!</bold></red>\n<gray>Reason: <white><reason></white></gray>\n<gray>Duration: <white><duration></white></gray>");
            Objects.requireNonNull(target.getPlayer()).kick(miniMessage(banScreen, reason, duration));
        }

        addHistory(target.getUniqueId(), "ban", reason, issuer.getName(), duration);
        broadcastStaff(YELLOW + issuer.getName() + " " + GRAY + "banned " + YELLOW + target.getName() + " " + GRAY + "for " + WHITE + (duration == null ? "permanent" : duration) + " " + GRAY + "(Reason: " + reason + ")");
    }

    public void blacklist(OfflinePlayer target, String reason, CommandSender issuer) {
        if (isAlreadyBlacklisted(target)) {
            issuer.sendMessage(RED + target.getName() + " is already blacklisted!");
            return;
        }

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress()).getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).addBan(ip, DARK_RED + "[BLACKLISTED] " + WHITE + reason, null, issuer.getName());
            storePlayerIP(target.getUniqueId(), ip);
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(target.getName()), DARK_RED + "[BLACKLISTED] " + WHITE + reason, null, issuer.getName());

        if (target.isOnline()) {
            String blacklistScreen = plugin.getMessagesConfig().getString("messages.blacklist-screen", "<dark_red><bold>BLACKLISTED</bold></dark_red>\n<gray>Reason: <white><reason></white></gray>\n<gray>Duration: <white><duration></white></gray>");
            Objects.requireNonNull(target.getPlayer()).kick(miniMessage(blacklistScreen, reason, "permanent"));
        }

        addHistory(target.getUniqueId(), "blacklist", reason, issuer.getName(), null);
        broadcastStaff(DARK_RED + issuer.getName() + " " + GRAY + "blacklisted " + DARK_RED + target.getName() + " " + GRAY + "(Reason: " + reason + ")");
    }

    public void kick(Player target, String reason, Player issuer) {
        target.kick(miniMessage("<red><bold>KICKED</bold></red>\n<gray>Reason: <white><reason></white></gray>", reason, null));

        addHistory(target.getUniqueId(), "kick", reason, issuer.getName(), null);
        broadcastStaff(YELLOW + issuer.getName() + " " + GRAY + "kicked " + YELLOW + target.getName() + " " + GRAY + "for " + WHITE + reason);
    }

    public void unmute(UUID playerUUID) {
        if (!isMuted(playerUUID)) {
            return;
        }
        mutedPlayers.remove(playerUUID);
    }

    public void unmuteByIP(String ip) {
        if (!isMutedByIP(ip)) {
            return;
        }
        mutedIPs.remove(ip);
    }

    public long parseDuration(String duration) {
        if (duration == null) {
            return 0;
        }

        long total = 0;
        StringBuilder current = new StringBuilder();

        for (char c : duration.toCharArray()) {
            if (Character.isDigit(c)) {
                current.append(c);
            } else {
                long amount = Long.parseLong(current.toString());
                switch (c) {
                    case 's':
                        total += TimeUnit.SECONDS.toMillis(amount);
                        break;
                    case 'm':
                        total += TimeUnit.MINUTES.toMillis(amount);
                        break;
                    case 'h':
                        total += TimeUnit.HOURS.toMillis(amount);
                        break;
                    case 'd':
                        total += TimeUnit.DAYS.toMillis(amount);
                        break;
                    default:
                        break;
                }
                current = new StringBuilder();
            }
        }

        return total;
    }

    public void storePlayerIP(UUID playerUUID, String ip) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO punishment_banned_ips (uuid, ip) VALUES (?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET ip = excluded.ip")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, ip);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to store banned player IP for " + playerUUID, e);
        }
    }

    public String getBannedPlayerIP(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT ip FROM punishment_banned_ips WHERE uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("ip") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load banned player IP for " + playerUUID, e);
        }
    }

    public void removeBannedPlayerIP(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM punishment_banned_ips WHERE uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove banned player IP for " + playerUUID, e);
        }
    }

    private void broadcastStaff(String message) {
        plugin.getNotificationService().sendStaffNotice(message);
    }
}
