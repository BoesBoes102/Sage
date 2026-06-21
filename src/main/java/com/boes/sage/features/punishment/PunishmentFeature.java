package com.boes.sage.features.punishment;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.punishment.listeners.PunishmentChatListener;
import com.boes.sage.features.punishment.listeners.PunishmentHistoryListener;

public class PunishmentFeature implements SageFeature {
    private PunishmentService service;

    @Override
    public void register(Sage plugin) {
        this.service = new PunishmentService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new PunishmentChatListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PunishmentHistoryListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public PunishmentService service() {
        return service;
    }
}
