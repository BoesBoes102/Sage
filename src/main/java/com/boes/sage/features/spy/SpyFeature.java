package com.boes.sage.features.spy;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.spy.listeners.CommandSpyListener;
import com.boes.sage.features.spy.listeners.ConsoleSpyListener;
import java.util.logging.Logger;

public class SpyFeature implements SageFeature {
    private SpyService service;
    private ConsoleSpyListener consoleSpyListener;

    @Override
    public void register(Sage plugin) {
        this.service = new SpyService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new CommandSpyListener(plugin), plugin);
        this.consoleSpyListener = new ConsoleSpyListener(plugin);
        Logger.getLogger("").addHandler(consoleSpyListener);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (consoleSpyListener != null) {
            Logger.getLogger("").removeHandler(consoleSpyListener);
            consoleSpyListener.cleanup();
        }
    }

    public SpyService service() {
        return service;
    }
}
