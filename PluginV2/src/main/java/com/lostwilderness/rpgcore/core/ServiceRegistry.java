package com.lostwilderness.rpgcore.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Typed registry for domain services so modules can depend on each other via interface.
 */
public final class ServiceRegistry {

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        Object o = services.get(type);
        return o != null ? (T) o : null;
    }

    public <T> void register(Class<T> type, T instance) {
        if (instance != null) {
            services.put(type, instance);
        }
    }
}
