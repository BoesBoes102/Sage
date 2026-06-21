package com.boes.sage.features.chatlog;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.chatlog.listeners.ChatLogListener;

public class ChatLogFeature implements SageFeature {
    private ChatLogService service;

    @Override
    public void register(Sage plugin) {
        this.service = new ChatLogService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChatLogListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (service != null) {
            service.saveLogs();
        }
    }

    public ChatLogService service() {
        return service;
    }
}
