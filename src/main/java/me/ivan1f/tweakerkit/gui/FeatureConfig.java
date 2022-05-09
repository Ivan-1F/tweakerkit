package me.ivan1f.tweakerkit.gui;

import com.google.common.collect.Maps;

import java.util.Map;

public class FeatureConfig {
    public static Key<Boolean> TRANSLATABLE_LABEL = new Key<>(false);
    public static Key<Boolean> BETTER_CONFIG_PANE = new Key<>(false);
    public static Key<Boolean> RIGHT_ALIGNED_PANE = new Key<>(false);

    private final Map<Key<?>, Object> data = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) data.getOrDefault(key, key.getDefaultValue());
    }

    public <T> void put(Key<T> key, T value) {
        data.put(key, value);
    }

    public static class Key<T> {
        private final T defaultValue;

        public T getDefaultValue() {
            return defaultValue;
        }

        public Key(T defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
