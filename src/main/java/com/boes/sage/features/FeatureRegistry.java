package com.boes.sage.features;

import java.util.LinkedHashMap;
import java.util.Map;

public class FeatureRegistry {
    private final Map<String, SageFeature> features = new LinkedHashMap<>();

    public void register(String key, SageFeature feature) {
        features.put(key, feature);
    }

    public SageFeature get(String key) {
        return features.get(key);
    }

    public Map<String, SageFeature> all() {
        return features;
    }
}
