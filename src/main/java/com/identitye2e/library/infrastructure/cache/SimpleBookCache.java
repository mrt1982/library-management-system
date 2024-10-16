package com.identitye2e.library.infrastructure.cache;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/*
Basic LRU cache, eviction is base on removing the eldest entry if size
exceeds the maxSize. Base on accessOrder.

//TODO: Further optimisation use Caffeine
Caffeine is a highly efficient and feature-rich Java cache library. It provides:

Concurrent access: It handles thread safety and concurrent updates internally, making it suitable for high-performance caching in multi-threaded environments.
Eviction policies: Caffeine supports different eviction strategies such as:
Size-based eviction: Evicts entries when the cache reaches a certain size.
Time-based eviction: Evicts entries that haven't been accessed for a specified time.
Access order: Evicts the least recently used (LRU) entries.
 */
public class SimpleBookCache<K,V> implements BookCache<K, V> {
    private final Map<K,V> cache;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public SimpleBookCache(int maxSize) {
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }

    @Override
    public Optional<V> get(K key) {
        readWriteLock.readLock().lock();
        try{
            return Optional.ofNullable(cache.get(key));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        readWriteLock.writeLock().lock();
        try{
            cache.put(key, value);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(K key, V value) {
        put(key, value);
    }

    @Override
    public void invalidate(K key) {
        readWriteLock.writeLock().lock();
        try{
            cache.remove(key);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
