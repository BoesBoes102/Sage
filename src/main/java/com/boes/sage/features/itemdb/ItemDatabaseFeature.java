package com.boes.sage.features.itemdb;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
public class ItemDatabaseFeature implements SageFeature {
    private ItemDatabaseService service;

    @Override
    public void register(Sage plugin) {
        this.service = new ItemDatabaseService(plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public ItemDatabaseService service() {
        return service;
    }
}
