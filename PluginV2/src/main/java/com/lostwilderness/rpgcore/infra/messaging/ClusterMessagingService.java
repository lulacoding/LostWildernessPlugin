package com.lostwilderness.rpgcore.infra.messaging;

import java.util.function.Consumer;

/**
 * Abstraction for cross-node messaging (Redis pub/sub, Bungee/Velocity channels, etc.).
 * Stub implementation does nothing; replace with real impl when needed.
 */
public interface ClusterMessagingService {

    void publish(String topic, byte[] payload);

    void subscribe(String topic, Consumer<byte[]> handler);

    void close();
}
