package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.data.PunishmentHistory;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PunishmentManager {
    private final Sage plugin;
    private final JsonStorageManager dataStorage;
    private final JsonStorageManager historyStorage;
    private final JsonStorageManager ipStorage;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final Map<String, Long> mutedIPs = new HashMap<>();

    public PunishmentManager(Sage plugin) {
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
            String warnTitle = plugin.getConfig().getString("messages.warn-title", "§e§lWARNED");
            String warnMessage = plugin.getConfig().getString("messages.warn-message", "§e§lYou have been warned!\n§7Reason: §f{reason}");
            warnMessage = warnMessage.replace("{reason}", reason);
            Objects.requireNonNull(target.getPlayer()).sendTitle(warnTitle, "§7" + reason, 10, 70, 20);
        } else {
            String warnTitle = plugin.getConfig().getString("messages.warn-title", "§e§lWARNED");
            String warnMessage = plugin.getConfig().getString("messages.warn-message", "§e§lYou have been warned!\n§7Reason: §f{reason}");
            plugin.getNotificationManager().addNotification(target.getUniqueId(), warnTitle, warnMessage, reason, "");
        }

        addHistory(target.getUniqueId(), "warn", reason, issuer.getName(), null);
        broadcastStaff("§e" + issuer.getName() + " §7warned §e" + target.getName() + " §7for §f" + reason);
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
            issuer.sendMessage("§c" + target.getName() + " is already muted!");
            return;
        }

        long muteExpiry = duration == null || duration.equalsIgnoreCase("permanent") ? -1 : System.currentTimeMillis() + parseDuration(duration);
        mutedPlayers.put(target.getUniqueId(), muteExpiry);

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress())).getAddress().getHostAddress();
            mutedIPs.put(ip, muteExpiry);

            String muteTitle = plugin.getConfig().getString("messages.mute-title", "§c§lMUTED");
            String muteMessage = plugin.getConfig().getString("messages.mute-message", "§c§lYou have been muted!\n§7Reason: §f{reason}\n§7Duration: §f{duration}");
            assert duration != null;
            muteMessage = muteMessage.replace("{reason}", reason).replace("{duration}", duration);
            target.getPlayer().sendTitle(muteTitle, muteMessage, 10, 70, 20);
        } else {
            String muteTitle = plugin.getConfig().getString("messages.mute-title", "§c§lMUTED");
            String muteMessage = plugin.getConfig().getString("messages.mute-message", "§c§lYou have been muted!\n§7Reason: §f{reason}\n§7Duration: §f{duration}");
            plugin.getNotificationManager().addNotification(target.getUniqueId(), muteTitle, muteMessage, reason, duration);
        }
        
        addHistory(target.getUniqueId(), "mute", reason, issuer.getName(), duration);
        broadcastStaff("§e" + issuer.getName() + " §7muted §e" + target.getName() + " §7for §f" + duration + " §7(Reason: " + reason + ")");
    }

    public void ban(OfflinePlayer target, String reason, String duration, Player issuer) {
        if (isAlreadyBanned(target)) {
            issuer.sendMessage("§c" + target.getName() + " is already banned!");
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
            String banMessage = plugin.getConfig().getString("messages.ban-screen", "§c§lYou have been banned!\n§7Reason: §f{reason}\n§7Duration: §f{duration}");
            banMessage = banMessage.replace("{reason}", reason).replace("{duration}", duration == null ? "permanent" : duration);
            Objects.requireNonNull(target.getPlayer()).kickPlayer(banMessage);
        }

        addHistory(target.getUniqueId(), "ban", reason, issuer.getName(), duration);
        broadcastStaff("§e" + issuer.getName() + " §7banned §e" + target.getName() + " §7for §f" + (duration == null ? "permanent" : duration) + " §7(Reason: " + reason + ")");
    }

    public void blacklist(OfflinePlayer target, String reason, Player issuer) {
        if (isAlreadyBlacklisted(target)) {
            issuer.sendMessage("§c" + target.getName() + " is already blacklisted!");
            return;
        }

        if (target.isOnline()) {
            String ip = Objects.requireNonNull(Objects.requireNonNull(target.getPlayer()).getAddress()).getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).addBan(ip, "§4[BLACKLISTED] §f" + reason, null, issuer.getName());
            storePlayerIP(target.getUniqueId(), ip);
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(target.getName()), "§4[BLACKLISTED] §f" + reason, null, issuer.getName());

        if (target.isOnline()) {
            String blacklistMessage = plugin.getConfig().getString("messages.blacklist-screen", "§4§lBLACKLISTED\n§7Reason: §f{reason}\n§7Duration: §f{duration}");
            blacklistMessage = blacklistMessage.replace("{reason}", reason).replace("{duration}", "permanent");
            Objects.requireNonNull(target.getPlayer()).kickPlayer(blacklistMessage);
        }

        addHistory(target.getUniqueId(), "blacklist", reason, issuer.getName(), null);
        broadcastStaff("§4" + issuer.getName() + " §7blacklisted §4" + target.getName() + " §7(Reason: " + reason + ")");
    }

    public void kick(Player target, String reason, Player issuer) {
        target.kickPlayer("§c§lKICKED\n§7Reason: §f" + reason);

        addHistory(target.getUniqueId(), "kick", reason, issuer.getName(), null);
        broadcastStaff("§e" + issuer.getName() + " §7kicked §e" + target.getName() + " §7for §f" + reason);
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
        if (duration == null) return 0;

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
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("sage.staff")) {
                p.sendMessage(message);
            }
        }
    }
}