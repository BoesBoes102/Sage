package com.boes.sage.features.warp;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
public class WarpFeature implements SageFeature {
    private WarpService service;

    @Override
    public void register(Sage plugin) {
        this.service = new WarpService(plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public WarpService service() {
        return service;
    }
}
