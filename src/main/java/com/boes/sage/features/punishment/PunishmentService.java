package com.boes.sage.features.punishment;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.boes.sage.features.punishment.data.PunishmentHistory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
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

    private final Sage plugin;
    private final JsonStorageManager dataStorage;
    private final JsonStorageManager historyStorage;
    private final JsonStorageManager ipStorage;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final Map<String, Long> mutedIPs = new HashMap<>();

    public PunishmentService(Sage plugin) {
        this.plugin = plugin;
        this.dataStorage = new JsonStorageManager(new File(plugin.getDataFolder(), "punishments.json"));
        this.historyStorage = new JsonStorageManager(new File(plugin.getDataFolder(), "history.json"));
        this.ipStorage = new JsonStorageManager(new File(plugin.getDataFolder(), "banned_ips.json"));
    }

    public int getPlayerStack(UUID uuid, String reason) {
        JsonObject json = dataStorage.load();
        if (json.has(uuid.toString())) {
            JsonObject playerObj = json.getAsJsonObject(uuid.toString());
            if (playerObj.has(reason)) {
                return playerObj.get(reason).getAsInt();
            }
        }
        return 0;
    }

    public void incrementStack(UUID uuid, String reason) {
        JsonObject json = dataStorage.load();
        JsonObject playerObj = json.has(uuid.toString()) ? json.getAsJsonObject(uuid.toString()) : new JsonObject();
        int current = getPlayerStack(uuid, reason);
        playerObj.addProperty(reason, current + 1);
        json.add(uuid.toString(), playerObj);
        dataStorage.save(json);
    }

    public void resetStack(UUID uuid, String reason) {
        JsonObject json = dataStorage.load();
        JsonObject playerObj = json.has(uuid.toString()) ? json.getAsJsonObject(uuid.toString()) : new JsonObject();
        playerObj.addProperty(reason, 0);
        json.add(uuid.toString(), playerObj);
        dataStorage.save(json);
    }

    private void addHistory(UUID uuid, String type, String reason, String punisher, String duration) {
        JsonObject json = historyStorage.load();
        JsonArray history = json.has(uuid.toString()) ? json.getAsJsonArray(uuid.toString()) : new JsonArray();

        JsonObject entry = new JsonObject();
        entry.addProperty("timestamp", System.currentTimeMillis());
        entry.addProperty("type", type);
        entry.addProperty("reason", reason);
        entry.addProperty("punisher", punisher);
        entry.addProperty("duration", duration == null ? "permanent" : duration);

        history.add(entry);
        json.add(uuid.toString(), history);
        historyStorage.save(json);
    }

    public List<PunishmentHistory> getPlayerHistory(UUID uuid) {
        List<PunishmentHistory> result = new ArrayList<>();
        JsonObject json = historyStorage.load();

        if (json.has(uuid.toString())) {
            JsonArray historyArray = json.getAsJsonArray(uuid.toString());
            for (int i = 0; i < historyArray.size(); i++) {
                try {
                    JsonObject entry = historyArray.get(i).getAsJsonObject();
                    long timestamp = entry.get("timestamp").getAsLong();
                    String type = entry.get("type").getAsString();
                    String reason = entry.get("reason").getAsString();
                    String punisher = entry.get("punisher").getAsString();
                    String duration = entry.get("duration").getAsString();

                    result.add(new PunishmentHistory(type, reason, punisher, timestamp, "permanent".equals(duration) ? null : duration, uuid));
                } catch (Exception ignored) {
                }
            }
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
        JsonObject json = historyStorage.load();

        if (!json.has(uuid.toString())) {
            return false;
        }

        JsonArray historyArray = json.getAsJsonArray(uuid.toString());
        boolean removed = false;

        for (int i = 0; i < historyArray.size(); i++) {
            try {
                JsonObject entry = historyArray.get(i).getAsJsonObject();
                if (entry.get("timestamp").getAsLong() == timestamp) {
                    historyArray.remove(i);
                    removed = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        if (removed) {
            json.add(uuid.toString(), historyArray);
            historyStorage.save(json);
        }

        return removed;
    }

    public boolean removeHistoryByIndex(UUID uuid, String type, int index) {
        List<PunishmentHistory> history = getHistoryByType(uuid, type);

        if (index < 0 || index >= history.size()) {
            return false;
        }

        PunishmentHistory toRemove = history.get(index);
        return removeHistoryByTimestamp(uuid, toRemove.getTimestamp());
    }

    public void warn(OfflinePlayer target, String reason, Player issuer) {
        plugin.getLogger().info(issuer.getName() + " warned " + target.getName() + " for " + reason);

        if (target.isOnline()) {
            String warnTitle = plugin.getMessagesConfig().getString("messages.warn-title", YELLOW + BOLD + "WARNED");
            String warnMessage = plugin.getMessagesConfig().getString("messages.warn-message", YELLOW + BOLD + "You have been warned!\n" + GRAY + "Reason: " + WHITE + "{reason}");
            warnMessage = warnMessage.replace("{reason}", reason);
            Objects.requireNonNull(target.getPlayer()).sendTitle(warnTitle, GRAY + reason, 10, 70, 20);
        } else {
            String warnTitle = plugin.getMessagesConfig().getString("messages.warn-title", YELLOW + BOLD + "WARNED");
            String warnMessage = plugin.getMessagesConfig().getString("messages.warn-message", YELLOW + BOLD + "You have been warned!\n" + GRAY + "Reason: " + WHITE + "{reason}");
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

    public void mute(OfflinePlayer target, String reason, String duration, Player issuer) {
        if (isAlreadyMuted(target.getUniqueId())) {
            issuer.sendMessage(RED + target.getName() + " is already muted!");
            return;
        }

        long muteExpiry = duration == null || duration.equalsIgnoreCase("permanent") ? -1 : System.currentTimeMillis() + parseDuration(duration);
        mutedPlayers.put(target.getUniqueId(), muteExpiry);

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress())).getAddress().getHostAddress();
            mutedIPs.put(ip, muteExpiry);

            String muteTitle = plugin.getMessagesConfig().getString("messages.mute-title", RED + BOLD + "MUTED");
            String muteMessage = plugin.getMessagesConfig().getString("messages.mute-message", RED + BOLD + "You have been muted!\n" + GRAY + "Reason: " + WHITE + "{reason}\n" + GRAY + "Duration: " + WHITE + "{duration}");
            String shownDuration = duration == null ? "permanent" : duration;
            muteMessage = muteMessage.replace("{reason}", reason).replace("{duration}", shownDuration);
            Objects.requireNonNull(target.getPlayer()).sendTitle(muteTitle, muteMessage, 10, 70, 20);
        } else {
            String muteTitle = plugin.getMessagesConfig().getString("messages.mute-title", RED + BOLD + "MUTED");
            String muteMessage = plugin.getMessagesConfig().getString("messages.mute-message", RED + BOLD + "You have been muted!\n" + GRAY + "Reason: " + WHITE + "{reason}\n" + GRAY + "Duration: " + WHITE + "{duration}");
            plugin.getNotificationService().addNotification(target.getUniqueId(), muteTitle, muteMessage, reason, duration == null ? "permanent" : duration);
        }

        addHistory(target.getUniqueId(), "mute", reason, issuer.getName(), duration);
        broadcastStaff(YELLOW + issuer.getName() + " " + GRAY + "muted " + YELLOW + target.getName() + " " + GRAY + "for " + WHITE + duration + " " + GRAY + "(Reason: " + reason + ")");
    }

    public void ban(OfflinePlayer target, String reason, String duration, Player issuer) {
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
            String banMessage = plugin.getMessagesConfig().getString("messages.ban-screen", RED + BOLD + "You have been banned!\n" + GRAY + "Reason: " + WHITE + "{reason}\n" + GRAY + "Duration: " + WHITE + "{duration}");
            banMessage = banMessage.replace("{reason}", reason).replace("{duration}", duration == null ? "permanent" : duration);
            Objects.requireNonNull(target.getPlayer()).kickPlayer(banMessage);
        }

        addHistory(target.getUniqueId(), "ban", reason, issuer.getName(), duration);
        broadcastStaff(YELLOW + issuer.getName() + " " + GRAY + "banned " + YELLOW + target.getName() + " " + GRAY + "for " + WHITE + (duration == null ? "permanent" : duration) + " " + GRAY + "(Reason: " + reason + ")");
    }

    public void blacklist(OfflinePlayer target, String reason, Player issuer) {
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
            String blacklistMessage = plugin.getMessagesConfig().getString("messages.blacklist-screen", DARK_RED + BOLD + "BLACKLISTED\n" + GRAY + "Reason: " + WHITE + "{reason}\n" + GRAY + "Duration: " + WHITE + "{duration}");
            blacklistMessage = blacklistMessage.replace("{reason}", reason).replace("{duration}", "permanent");
            Objects.requireNonNull(target.getPlayer()).kickPlayer(blacklistMessage);
        }

        addHistory(target.getUniqueId(), "blacklist", reason, issuer.getName(), null);
        broadcastStaff(DARK_RED + issuer.getName() + " " + GRAY + "blacklisted " + DARK_RED + target.getName() + " " + GRAY + "(Reason: " + reason + ")");
    }

    public void kick(Player target, String reason, Player issuer) {
        target.kickPlayer(RED + BOLD + "KICKED\n" + GRAY + "Reason: " + WHITE + reason);

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
        JsonObject json = ipStorage.load();
        json.addProperty(playerUUID.toString(), ip);
        ipStorage.save(json);
    }

    public String getBannedPlayerIP(UUID playerUUID) {
        JsonObject json = ipStorage.load();
        if (json.has(playerUUID.toString())) {
            return json.get(playerUUID.toString()).getAsString();
        }
        return null;
    }

    public void removeBannedPlayerIP(UUID playerUUID) {
        JsonObject json = ipStorage.load();
        if (json.has(playerUUID.toString())) {
            json.remove(playerUUID.toString());
            ipStorage.save(json);
        }
    }

    private void broadcastStaff(String message) {
        plugin.getNotificationService().sendStaffNotice(message);
    }
}
