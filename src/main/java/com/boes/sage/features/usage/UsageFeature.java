package com.boes.sage.features.usage;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.usage.listeners.UsagePlayerJoinListener;
import com.boes.sage.features.usage.listeners.UsagePlayerQuitListener;

public class UsageFeature implements SageFeature {
    private UsageBossBarService service;

    @Override
    public void register(Sage plugin) {
        this.service = new UsageBossBarService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new UsagePlayerJoinListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UsagePlayerQuitListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (service != null) {
            service.cleanup();
        }
    }

    public UsageBossBarService service() {
        return service;
    }
}
