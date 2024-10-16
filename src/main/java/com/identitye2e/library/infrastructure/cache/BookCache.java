package com.identitye2e.library.infrastructure.cache;

import java.util.Optional;
public interface BookCache<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);

    void update(K key, V value);

    void invalidate(K key);
}
