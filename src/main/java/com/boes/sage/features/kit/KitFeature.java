package com.boes.sage.features.kit;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.kit.listeners.KitGuiListener;
public class KitFeature implements SageFeature {
    private KitService service;

    @Override
    public void register(Sage plugin) {
        this.service = new KitService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new KitGuiListener(), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public KitService service() {
        return service;
    }
}
