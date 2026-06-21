package com.boes.sage.features.notification;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class NotificationService {
    private static final String BOSSBAR_PATH = "notifications.bossbar";
    private static final String ANNOUNCEMENTS_PATH = "notifications.announcements";
    private static final String STAFF_CHANNEL_PATH = "notifications.channels.staff";
    private static final String ADMIN_CHANNEL_PATH = "notifications.channels.admin";
    private static final Component ANNOUNCEMENT_BARS = Component.text("=========");

    private final Sage plugin;
    private final JsonStorageManager storageManager;
    private final MiniMessage miniMessage;
    private final HttpClient httpClient;
    private BossBar globalBossBar;
    private BukkitTask bossBarRotationTask;
    private BukkitTask announcementTask;
    private List<String> bossBarMessages;
    private List<String> announcementMessages;
    private int bossBarIndex;
    private int announcementIndex;

    public NotificationService(Sage plugin) {
        this.plugin = plugin;
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "notifications.json"));
        this.miniMessage = MiniMessage.miniMessage();
        this.httpClient = HttpClient.newHttpClient();
        this.bossBarMessages = List.of();
        this.announcementMessages = List.of();

        startConfiguredBroadcasts();
    }

    public void addNotification(UUID uuid, String title, String message, String reason, String duration) {
        JsonObject json = storageManager.load();
        String uuidStr = uuid.toString();

        JsonArray notificationsArray = json.has(uuidStr) ? json.getAsJsonArray(uuidStr) : new JsonArray();

        JsonObject notificationObj = new JsonObject();
        notificationObj.addProperty("title", title);
        notificationObj.addProperty("message", message);
        notificationObj.addProperty("reason", reason);
        notificationObj.addProperty("duration", duration);

        notificationsArray.add(notificationObj);
        json.add(uuidStr, notificationsArray);

        storageManager.save(json);
    }

    public void deliverNotifications(Player player) {
        UUID uuid = player.getUniqueId();
        JsonObject json = storageManager.load();
        String uuidStr = uuid.toString();

        if (!json.has(uuidStr)) {
            return;
        }

        JsonArray notificationsArray = json.getAsJsonArray(uuidStr);

        for (int i = 0; i < notificationsArray.size(); i++) {
            try {
                JsonObject notificationObj = notificationsArray.get(i).getAsJsonObject();
                String title = notificationObj.get("title").getAsString();
                String message = notificationObj.get("message").getAsString();
                String reason = notificationObj.get("reason").getAsString();
                String duration = notificationObj.get("duration").getAsString();

                message = message.replace("{reason}", reason);
                message = message.replace("{duration}", duration);
                title = title.replace("{reason}", reason);
                title = title.replace("{duration}", duration);

                player.sendMessage("");
                player.sendMessage(message);
                player.sendMessage("");

                if (!title.isEmpty()) {
                    player.sendTitle(title, "§7" + reason, 10, 70, 20);
                }
            } catch (Exception ignored) {
            }
        }

        json.remove(uuidStr);
        storageManager.save(json);
    }

    public void handlePlayerJoin(Player player) {
        if (globalBossBar != null) {
            player.showBossBar(globalBossBar);
        }
    }

    public void sendStaffChat(HumanEntity sender, String message) {
        sendChannelMessage("SC", "\u00A7b", sender.getName(), message);
    }

    public void sendAdminChat(HumanEntity sender, String message) {
        String formattedMessage = formatChannelMessage("AC", "\u00A7c", sender.getName(), message);
        sendToOnlineStaff(formattedMessage, "sage.staff");
        String plainMessage = stripColor(formattedMessage);
        sendWebhook(STAFF_CHANNEL_PATH + ".webhook-url", "Staff Channel", plainMessage);
        sendWebhook(ADMIN_CHANNEL_PATH + ".webhook-url", "Admin Channel", plainMessage);
    }

    public void sendStaffAnnouncement(String senderName, String message) {
        String formattedMessage = ChatChannel.STAFF_ANNOUNCEMENT.format(senderName, message);
        sendStaffNotice(formattedMessage);
    }

    public void sendStaffNotice(String message) {
        sendToOnlineStaff(message, "sage.staff");
        sendWebhook(STAFF_CHANNEL_PATH + ".webhook-url", "Staff Channel", stripColor(message));
    }

    public void shutdown() {
        cancelTask(bossBarRotationTask);
        cancelTask(announcementTask);
        bossBarRotationTask = null;
        announcementTask = null;

        if (globalBossBar != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hideBossBar(globalBossBar);
            }
            globalBossBar = null;
        }
    }

    private void startConfiguredBroadcasts() {
        configureBossBar();
        configureAnnouncements();
    }

    private void configureBossBar() {
        cancelTask(bossBarRotationTask);
        bossBarRotationTask = null;
        bossBarMessages = sanitizeMessages(plugin.getConfig().getStringList(BOSSBAR_PATH + ".messages"));
        bossBarIndex = 0;

        if (globalBossBar != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hideBossBar(globalBossBar);
            }
            globalBossBar = null;
        }

        if (!plugin.getConfig().getBoolean(BOSSBAR_PATH + ".enabled", false) || bossBarMessages.isEmpty()) {
            return;
        }

        globalBossBar = BossBar.bossBar(
            deserialize(bossBarMessages.get(0)),
            readBossBarProgress(plugin.getConfig()),
            readBossBarColor(plugin.getConfig().getString(BOSSBAR_PATH + ".color", "WHITE")),
            readBossBarOverlay(plugin.getConfig().getString(BOSSBAR_PATH + ".overlay", "PROGRESS"))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(globalBossBar);
        }

        if (bossBarMessages.size() > 1) {
            long intervalTicks = Math.max(20L, plugin.getConfig().getLong(BOSSBAR_PATH + ".switch-seconds", 10L) * 20L);
            bossBarRotationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::advanceBossBar, intervalTicks, intervalTicks);
        }
    }

    private void configureAnnouncements() {
        cancelTask(announcementTask);
        announcementTask = null;
        announcementMessages = sanitizeMessages(plugin.getConfig().getStringList(ANNOUNCEMENTS_PATH + ".messages"));
        announcementIndex = 0;

        if (!plugin.getConfig().getBoolean(ANNOUNCEMENTS_PATH + ".enabled", false) || announcementMessages.isEmpty()) {
            return;
        }

        long intervalTicks = Math.max(20L, plugin.getConfig().getLong(ANNOUNCEMENTS_PATH + ".interval-seconds", 300L) * 20L);
        announcementTask = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastNextAnnouncement, intervalTicks, intervalTicks);
    }

    private void advanceBossBar() {
        if (globalBossBar == null || bossBarMessages.size() <= 1) {
            return;
        }

        bossBarIndex = (bossBarIndex + 1) % bossBarMessages.size();
        globalBossBar.name(deserialize(bossBarMessages.get(bossBarIndex)));
    }

    private void broadcastNextAnnouncement() {
        if (announcementMessages.isEmpty()) {
            return;
        }

        String message = announcementMessages.get(announcementIndex);
        announcementIndex = (announcementIndex + 1) % announcementMessages.size();

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            return;
        }

        Component parsedMessage = deserialize(message);
        for (Player player : onlinePlayers) {
            player.sendMessage(ANNOUNCEMENT_BARS);
            player.sendMessage(parsedMessage);
            player.sendMessage(ANNOUNCEMENT_BARS);
        }
    }

    private List<String> sanitizeMessages(List<String> messages) {
        List<String> sanitized = new ArrayList<>();
        for (String message : messages) {
            if (message == null) {
                continue;
            }

            String trimmed = message.trim();
            if (!trimmed.isEmpty()) {
                sanitized.add(trimmed);
            }
        }
        return sanitized;
    }

    private Component deserialize(String text) {
        String value = text == null ? "" : text;
        try {
            return miniMessage.deserialize(value);
        } catch (Exception exception) {
            return Component.text(value);
        }
    }

    private float readBossBarProgress(FileConfiguration config) {
        double progress = config.getDouble(BOSSBAR_PATH + ".progress", 1.0D);
        return (float) Math.max(0.0D, Math.min(1.0D, progress));
    }

    private BossBar.Color readBossBarColor(String value) {
        try {
            return BossBar.Color.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.WHITE;
        }
    }

    private BossBar.Overlay readBossBarOverlay(String value) {
        try {
            return BossBar.Overlay.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private void cancelTask(BukkitTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    private void sendChannelMessage(String label, String colorCode, String senderName, String message) {
        String formattedMessage = formatChannelMessage(label, colorCode, senderName, message);
        sendToOnlineStaff(formattedMessage, "sage.staff");
        sendWebhook(STAFF_CHANNEL_PATH + ".webhook-url", "Staff Channel", stripColor(formattedMessage));
    }

    private String formatChannelMessage(String label, String colorCode, String senderName, String message) {
        return "\u00A78[\u00A7r" + colorCode + label + "\u00A78] \u00A7f" + senderName + ": \u00A7f" + message;
    }

    private void sendToOnlineStaff(String message, String permission) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(permission)) {
                onlinePlayer.sendMessage(message);
            }
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }

    private void sendWebhook(String path, String username, String content) {
        String webhookUrl = plugin.getConfig().getString(path, "").trim();
        if (webhookUrl.isEmpty()) {
            return;
        }

        try {
            String payload = "{\"username\":\"" + escapeJson(username) + "\",\"content\":\"" + escapeJson(content) + "\"}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(throwable -> {
                    plugin.getLogger().warning("Failed to send staff webhook message: " + throwable.getMessage());
                    return null;
                });
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Invalid staff webhook URL at " + path);
        }
    }

    private String stripColor(String message) {
        return message.replaceAll("\u00A7.", "");
    }

    private String escapeJson(String value) {
        return Objects.requireNonNullElse(value, "")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }

    private enum ChatChannel {
        STAFF_ANNOUNCEMENT("SA", "\u00A7d");

        private final String label;
        private final String colorCode;

        ChatChannel(String label, String colorCode) {
            this.label = label;
            this.colorCode = colorCode;
        }

        private String format(String senderName, String message) {
            return "\u00A78[\u00A7r" + colorCode + label + "\u00A78] \u00A7f" + senderName + ": \u00A7f" + message;
        }
    }
}
