package com.boes.sage.features.back;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import org.bukkit.Bukkit;

public class BackFeature implements SageFeature {
    private BackService service;

    @Override
    public void register(Sage plugin) {
        this.service = new BackService();
        Bukkit.getPluginManager().registerEvents(new BackTeleportListener(service), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public BackService service() {
        return service;
    }
}
