package com.boes.sage.features.openinv;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.openinv.listeners.OpenEnderChestListener;
import com.boes.sage.features.openinv.listeners.OpenInventoryListener;

public class OpenInvFeature implements SageFeature {
    private OpenInventoryService inventoryService;
    private OpenEnderChestService enderChestService;

    @Override
    public void register(Sage plugin) {
        this.inventoryService = new OpenInventoryService(plugin);
        this.enderChestService = new OpenEnderChestService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new OpenInventoryListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new OpenEnderChestListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (inventoryService != null) {
            inventoryService.cleanup();
        }
        if (enderChestService != null) {
            enderChestService.cleanup();
        }
    }

    public OpenInventoryService inventoryService() {
        return inventoryService;
    }

    public OpenEnderChestService enderChestService() {
        return enderChestService;
    }
}
