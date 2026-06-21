package com.boes.sage.features.itemedit;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;

public class ItemEditFeature implements SageFeature {
    private ItemEditService service;

    @Override
    public void register(Sage plugin) {
        this.service = new ItemEditService();
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public ItemEditService service() {
        return service;
    }
}
