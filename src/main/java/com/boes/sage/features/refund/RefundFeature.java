package com.boes.sage.features.refund;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.refund.listeners.RefundCaptureListener;
public class RefundFeature implements SageFeature {
    private RefundService service;

    @Override
    public void register(Sage plugin) {
        this.service = new RefundService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new RefundCaptureListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public RefundService service() {
        return service;
    }
}
