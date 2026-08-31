package com.boes.sage.features.spy;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.spy.listeners.CommandSpyListener;

public class SpyFeature implements SageFeature {
    private SpyService service;

    @Override
    public void register(Sage plugin) {
        this.service = new SpyService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new CommandSpyListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public SpyService service() {
        return service;
    }
}
