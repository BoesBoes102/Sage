package com.boes.sage.features.notification;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.notification.listeners.ChatMuteListener;
import com.boes.sage.features.notification.listeners.NotificationJoinListener;

public class NotificationFeature implements SageFeature {
    private NotificationService service;

    @Override
    public void register(Sage plugin) {
        this.service = new NotificationService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new NotificationJoinListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChatMuteListener(), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (service != null) {
            service.shutdown();
        }
    }

    public NotificationService service() {
        return service;
    }
}
