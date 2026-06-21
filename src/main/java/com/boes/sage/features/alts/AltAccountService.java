package com.boes.sage.features.alts;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AltAccountService {
    private static final String PLAYERS_KEY = "players";
    private static final String IPS_KEY = "ips";

    private final JsonStorageManager storageManager;

    public AltAccountService(Sage plugin) {
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "alt-accounts.json"));
    }

    public synchronized void trackLogin(Player player) {
        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        String ip = player.getAddress().getAddress().getHostAddress();
        JsonObject root = storageManager.load();
        JsonObject players = getOrCreateObject(root, PLAYERS_KEY);
        JsonObject ips = getOrCreateObject(root, IPS_KEY);

        JsonObject playerRecord = players.has(uuid.toString()) && players.get(uuid.toString()).isJsonObject()
                ? players.getAsJsonObject(uuid.toString())
                : new JsonObject();

        playerRecord.addProperty("lastKnownName", player.getName());
        playerRecord.addProperty("lastIp", ip);
        addUnique(playerRecord, "ips", ip);
        players.add(uuid.toString(), playerRecord);

        addUnique(ips, ip, uuid.toString());

        root.add(PLAYERS_KEY, players);
        root.add(IPS_KEY, ips);
        storageManager.save(root);
    }

    public synchronized AltPlayerRecord getPlayerRecord(UUID uuid) {
        JsonObject root = storageManager.load();
        JsonObject players = getOrCreateObject(root, PLAYERS_KEY);
        JsonObject playerRecord = players.has(uuid.toString()) && players.get(uuid.toString()).isJsonObject()
                ? players.getAsJsonObject(uuid.toString())
                : null;

        if (playerRecord == null) {
            return null;
        }

        String lastKnownName = getString(playerRecord, "lastKnownName");
        String lastIp = getString(playerRecord, "lastIp");
        Set<String> ips = readStringSet(playerRecord.getAsJsonArray("ips"));
        return new AltPlayerRecord(uuid, resolveName(uuid, lastKnownName), lastIp, ips);
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
        JsonObject root = storageManager.load();
        JsonObject ips = getOrCreateObject(root, IPS_KEY);
        Map<String, List<AltMatch>> matchesByIp = new LinkedHashMap<>();

        for (String ip : ipsToCheck) {
            if (!ips.has(ip) || !ips.get(ip).isJsonArray()) {
                continue;
            }

            Set<UUID> seen = new LinkedHashSet<>();
            List<AltMatch> matches = new ArrayList<>();

            for (String uuidText : readStringSet(ips.getAsJsonArray(ip))) {
                UUID linkedUuid;
                try {
                    linkedUuid = UUID.fromString(uuidText);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                if (linkedUuid.equals(targetUuid) || !seen.add(linkedUuid)) {
                    continue;
                }

                AltPlayerRecord linkedRecord = getPlayerRecord(linkedUuid);
                String name = linkedRecord != null ? linkedRecord.name() : resolveName(linkedUuid, null);
                boolean online = Bukkit.getPlayer(linkedUuid) != null;
                matches.add(new AltMatch(linkedUuid, name, online));
            }

            if (!matches.isEmpty()) {
                matchesByIp.put(ip, matches);
            }
        }

        return matchesByIp;
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

    private JsonObject getOrCreateObject(JsonObject root, String key) {
        if (root.has(key) && root.get(key).isJsonObject()) {
            return root.getAsJsonObject(key);
        }
        JsonObject created = new JsonObject();
        root.add(key, created);
        return created;
    }

    private void addUnique(JsonObject parent, String key, String value) {
        JsonArray array = parent.has(key) && parent.get(key).isJsonArray()
                ? parent.getAsJsonArray(key)
                : new JsonArray();

        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && value.equals(element.getAsString())) {
                parent.add(key, array);
                return;
            }
        }

        array.add(value);
        parent.add(key, array);
    }

    private Set<String> readStringSet(JsonArray array) {
        Set<String> values = new LinkedHashSet<>();
        if (array == null) {
            return values;
        }

        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }

    private String getString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }

    public record AltPlayerRecord(UUID uuid, String name, String lastIp, Set<String> ips) {
    }

    public record AltMatch(UUID uuid, String name, boolean online) {
    }
}
