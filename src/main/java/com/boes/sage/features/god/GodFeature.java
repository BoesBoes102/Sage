package com.boes.sage.features.god;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.god.listeners.GodListener;

public class GodFeature implements SageFeature {
    private GodService service;

    @Override
    public void register(Sage plugin) {
        this.service = new GodService();
        plugin.getServer().getPluginManager().registerEvents(new GodListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public GodService service() {
        return service;
    }
}
