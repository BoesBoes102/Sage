package com.boes.sage.features.messaging;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;

public class MessagingFeature implements SageFeature {
    private MessagingService service;

    @Override
    public void register(Sage plugin) {
        this.service = new MessagingService(plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
    }

    public MessagingService service() {
        return service;
    }
}
