package com.boes.sage.features.staffmode;

import com.boes.sage.Sage;
import com.boes.sage.features.SageFeature;
import com.boes.sage.features.staffmode.listeners.StaffModeListener;

public class StaffModeFeature implements SageFeature {
    private StaffModeService service;

    @Override
    public void register(Sage plugin) {
        this.service = new StaffModeService(plugin);
        plugin.getServer().getPluginManager().registerEvents(new StaffModeListener(plugin), plugin);
    }

    @Override
    public void shutdown(Sage plugin) {
        if (service != null) {
            service.disableAllStaffMode();
        }
    }

    public StaffModeService service() {
        return service;
    }
}
