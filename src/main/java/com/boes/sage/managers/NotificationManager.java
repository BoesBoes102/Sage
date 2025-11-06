package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class NotificationManager {
    private final JsonStorageManager storageManager;

    public NotificationManager(Sage plugin) {
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "notifications.json"));
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
}