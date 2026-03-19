package com.lostwilderness.rpgcore.infra.messaging;

import java.util.function.Consumer;

/**
 * No-op implementation. Use when Redis/proxy messaging is not yet in place.
 */
public final class StubClusterMessagingService implements ClusterMessagingService {

    @Override
    public void publish(String topic, byte[] payload) {
        // no-op
    }

    @Override
    public void subscribe(String topic, Consumer<byte[]> handler) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }
}
