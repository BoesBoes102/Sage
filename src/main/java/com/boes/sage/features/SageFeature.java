package com.boes.sage.features;

import com.boes.sage.Sage;

public interface SageFeature {
    void register(Sage plugin);
    void shutdown(Sage plugin);
}
