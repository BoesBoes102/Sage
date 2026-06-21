package com.boes.sage.features.freeze;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.freeze.listeners.FreezeListener;

public class FreezeFeature implements SageFeature {
    private FreezeService service;

    @Override
    public void register(Sage plugin) {
        this.service = new FreezeService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new FreezeListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (service != null) {
            service.shutdown();
        }
    }

    public FreezeService service() {
        return service;
    }
}
