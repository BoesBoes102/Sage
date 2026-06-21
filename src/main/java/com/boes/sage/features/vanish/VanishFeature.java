package com.boes.sage.features.vanish;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.vanish.listeners.VanishListener;

public class VanishFeature implements SageFeature {
    private VanishService service;

    @Override
    public void register(Sage plugin) {
        this.service = new VanishService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new VanishListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public VanishService service() {
        return service;
    }
}
