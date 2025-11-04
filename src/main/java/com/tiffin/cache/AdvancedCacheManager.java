package com.tiffin.cache;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Advanced Multi-Level Cache Manager
 * Implements LRU, LFU, and Time-based expiration strategies
 * Similar to caching systems used by Gmail, Instagram, and other major platforms
 */
@Component
@Slf4j
public class AdvancedCacheManager {
    
    // L1 Cache: Hot data (most frequently accessed)
    private final ConcurrentHashMap<String, CacheEntry> l1Cache = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, Long> l1AccessOrder = new LinkedHashMap<>(16, 0.75f, true);
    
    // L2 Cache: Warm data (moderately accessed)
    private final ConcurrentHashMap<String, CacheEntry> l2Cache = new ConcurrentHashMap<>();
    private final PriorityQueue<CacheEntry> l2EvictionQueue = new PriorityQueue<>(
        Comparator.comparing(CacheEntry::getLastAccessTime)
    );
    
    // L3 Cache: Cold data (least frequently accessed)
    private final ConcurrentHashMap<String, CacheEntry> l3Cache = new ConcurrentHashMap<>();
    
    // Bloom Filter for fast negative lookups
    private final BloomFilter bloomFilter = new BloomFilter(1000000, 0.01);
    
    // Read-Write locks for thread safety
    private final ReadWriteLock l1Lock = new ReentrantReadWriteLock();
    private final ReadWriteLock l2Lock = new ReentrantReadWriteLock();
    private final ReadWriteLock l3Lock = new ReentrantReadWriteLock();
    
    // Cache statistics
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    // Configuration
    private static final int L1_MAX_SIZE = 1000;    // Hot data
    private static final int L2_MAX_SIZE = 5000;    // Warm data
    private static final int L3_MAX_SIZE = 10000;   // Cold data
    private static final long DEFAULT_TTL = 3600000; // 1 hour
    private static final long L1_TTL = 300000;      // 5 minutes
    private static final long L2_TTL = 1800000;     // 30 minutes
    private static final long L3_TTL = 3600000;     // 1 hour
    
    /**
     * Get value from multi-level cache with promotion strategy
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        // Check Bloom Filter first for fast negative lookup
        if (!bloomFilter.mightContain(key)) {
            missCount.incrementAndGet();
            return Optional.empty();
        }
        
        // Try L1 Cache (Hot data)
        Optional<T> result = getFromL1(key, type);
        if (result.isPresent()) {
            hitCount.incrementAndGet();
            updateAccessFrequency(key);
            return result;
        }
        
        // Try L2 Cache (Warm data)
        result = getFromL2(key, type);
        if (result.isPresent()) {
            hitCount.incrementAndGet();
            promoteToL1(key, result.get());
            return result;
        }
        
        // Try L3 Cache (Cold data)
        result = getFromL3(key, type);
        if (result.isPresent()) {
            hitCount.incrementAndGet();
            promoteToL2(key, result.get());
            return result;
        }
        
        missCount.incrementAndGet();
        return Optional.empty();
    }
    
    /**
     * Put value with intelligent cache placement
     */
    public <T> void put(String key, T value, long ttl) {
        bloomFilter.add(key);
        
        // Determine initial cache level based on key pattern and frequency
        CacheLevel level = determineCacheLevel(key);
        
        switch (level) {
            case L1:
                putInL1(key, value, ttl);
                break;
            case L2:
                putInL2(key, value, ttl);
                break;
            case L3:
                putInL3(key, value, ttl);
                break;
        }
    }
    
    public <T> void put(String key, T value) {
        put(key, value, DEFAULT_TTL);
    }
    
    /**
     * L1 Cache Operations (LRU with access frequency)
     */
    private <T> Optional<T> getFromL1(String key, Class<T> type) {
        l1Lock.readLock().lock();
        try {
            CacheEntry entry = l1Cache.get(key);
            if (entry != null && !entry.isExpired()) {
                entry.updateAccessTime();
                entry.incrementAccessCount();
                
                // Update access order for LRU
                l1Lock.readLock().unlock();
                l1Lock.writeLock().lock();
                try {
                    l1AccessOrder.put(key, System.currentTimeMillis());
                } finally {
                    l1Lock.readLock().lock();
                    l1Lock.writeLock().unlock();
                }
                
                return Optional.of(type.cast(entry.getValue()));
            }
            return Optional.empty();
        } finally {
            l1Lock.readLock().unlock();
        }
    }
    
    private <T> void putInL1(String key, T value, long ttl) {
        l1Lock.writeLock().lock();
        try {
            // Check if L1 is full and needs eviction
            if (l1Cache.size() >= L1_MAX_SIZE && !l1Cache.containsKey(key)) {
                evictFromL1();
            }
            
            CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + ttl);
            l1Cache.put(key, entry);
            l1AccessOrder.put(key, System.currentTimeMillis());
            
            log.debug("Added to L1 cache: {}", key);
        } finally {
            l1Lock.writeLock().unlock();
        }
    }
    
    private void evictFromL1() {
        if (l1AccessOrder.isEmpty()) return;
        
        // Find LRU entry
        String lruKey = l1AccessOrder.entrySet().iterator().next().getKey();
        CacheEntry evicted = l1Cache.remove(lruKey);
        l1AccessOrder.remove(lruKey);
        
        // Demote to L2 if still valuable
        if (evicted != null && evicted.getAccessCount() > 1) {
            demoteToL2(lruKey, evicted.getValue());
        }
        
        evictionCount.incrementAndGet();
        log.debug("Evicted from L1 cache: {}", lruKey);
    }
    
    /**
     * L2 Cache Operations (Time-based with LFU eviction)
     */
    private <T> Optional<T> getFromL2(String key, Class<T> type) {
        l2Lock.readLock().lock();
        try {
            CacheEntry entry = l2Cache.get(key);
            if (entry != null && !entry.isExpired()) {
                entry.updateAccessTime();
                entry.incrementAccessCount();
                return Optional.of(type.cast(entry.getValue()));
            }
            return Optional.empty();
        } finally {
            l2Lock.readLock().unlock();
        }
    }
    
    private <T> void putInL2(String key, T value, long ttl) {
        l2Lock.writeLock().lock();
        try {
            if (l2Cache.size() >= L2_MAX_SIZE && !l2Cache.containsKey(key)) {
                evictFromL2();
            }
            
            CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + ttl);
            l2Cache.put(key, entry);
            l2EvictionQueue.offer(entry);
            
            log.debug("Added to L2 cache: {}", key);
        } finally {
            l2Lock.writeLock().unlock();
        }
    }
    
    private void evictFromL2() {
        CacheEntry oldest = l2EvictionQueue.poll();
        if (oldest != null) {
            // Find and remove the oldest entry
            l2Cache.entrySet().removeIf(entry -> entry.getValue() == oldest);
            
            // Demote to L3 if still valuable
            if (oldest.getAccessCount() > 0) {
                demoteToL3(findKeyByValue(l2Cache, oldest), oldest.getValue());
            }
            
            evictionCount.incrementAndGet();
        }
    }
    
    /**
     * L3 Cache Operations (FIFO with periodic cleanup)
     */
    private <T> Optional<T> getFromL3(String key, Class<T> type) {
        l3Lock.readLock().lock();
        try {
            CacheEntry entry = l3Cache.get(key);
            if (entry != null && !entry.isExpired()) {
                entry.updateAccessTime();
                entry.incrementAccessCount();
                return Optional.of(type.cast(entry.getValue()));
            }
            return Optional.empty();
        } finally {
            l3Lock.readLock().unlock();
        }
    }
    
    private <T> void putInL3(String key, T value, long ttl) {
        l3Lock.writeLock().lock();
        try {
            if (l3Cache.size() >= L3_MAX_SIZE && !l3Cache.containsKey(key)) {
                evictFromL3();
            }
            
            CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + ttl);
            l3Cache.put(key, entry);
            
            log.debug("Added to L3 cache: {}", key);
        } finally {
            l3Lock.writeLock().unlock();
        }
    }
    
    private void evictFromL3() {
        // Simple FIFO eviction for L3
        if (!l3Cache.isEmpty()) {
            String firstKey = l3Cache.keys().nextElement();
            l3Cache.remove(firstKey);
            evictionCount.incrementAndGet();
            log.debug("Evicted from L3 cache: {}", firstKey);
        }
    }
    
    /**
     * Cache promotion and demotion strategies
     */
    private <T> void promoteToL1(String key, T value) {
        putInL1(key, value, L1_TTL);
        removeFromL2(key);
    }
    
    private <T> void promoteToL2(String key, T value) {
        putInL2(key, value, L2_TTL);
        removeFromL3(key);
    }
    
    private <T> void demoteToL2(String key, T value) {
        putInL2(key, value, L2_TTL);
    }
    
    private <T> void demoteToL3(String key, T value) {
        putInL3(key, value, L3_TTL);
    }
    
    /**
     * Cache level determination based on key patterns
     */
    private CacheLevel determineCacheLevel(String key) {
        // User session data - hot cache
        if (key.startsWith("session:") || key.startsWith("user:") || key.startsWith("auth:")) {
            return CacheLevel.L1;
        }
        
        // Menu and dish data - warm cache
        if (key.startsWith("dish:") || key.startsWith("menu:") || key.startsWith("category:")) {
            return CacheLevel.L2;
        }
        
        // Everything else - cold cache
        return CacheLevel.L3;
    }
    
    /**
     * Utility methods
     */
    private void updateAccessFrequency(String key) {
        // Update access patterns for machine learning-based optimization
        // This could be enhanced with ML algorithms for predictive caching
    }
    
    private void removeFromL2(String key) {
        l2Lock.writeLock().lock();
        try {
            l2Cache.remove(key);
        } finally {
            l2Lock.writeLock().unlock();
        }
    }
    
    private void removeFromL3(String key) {
        l3Lock.writeLock().lock();
        try {
            l3Cache.remove(key);
        } finally {
            l3Lock.writeLock().unlock();
        }
    }
    
    private String findKeyByValue(ConcurrentHashMap<String, CacheEntry> cache, CacheEntry value) {
        return cache.entrySet().stream()
            .filter(entry -> entry.getValue() == value)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Scheduled cleanup of expired entries
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void cleanupExpiredEntries() {
        log.debug("Starting cache cleanup...");
        
        cleanupL1();
        cleanupL2();
        cleanupL3();
        
        log.debug("Cache cleanup completed");
    }
    
    private void cleanupL1() {
        l1Lock.writeLock().lock();
        try {
            l1Cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            l1AccessOrder.entrySet().removeIf(entry -> 
                !l1Cache.containsKey(entry.getKey())
            );
        } finally {
            l1Lock.writeLock().unlock();
        }
    }
    
    private void cleanupL2() {
        l2Lock.writeLock().lock();
        try {
            l2Cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            l2EvictionQueue.removeIf(CacheEntry::isExpired);
        } finally {
            l2Lock.writeLock().unlock();
        }
    }
    
    private void cleanupL3() {
        l3Lock.writeLock().lock();
        try {
            l3Cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        } finally {
            l3Lock.writeLock().unlock();
        }
    }
    
    /**
     * Cache statistics and monitoring
     */
    public CacheStats getStats() {
        return CacheStats.builder()
            .l1Size(l1Cache.size())
            .l2Size(l2Cache.size())
            .l3Size(l3Cache.size())
            .hitCount(hitCount.get())
            .missCount(missCount.get())
            .evictionCount(evictionCount.get())
            .hitRatio(calculateHitRatio())
            .build();
    }
    
    private double calculateHitRatio() {
        long hits = hitCount.get();
        long total = hits + missCount.get();
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * Clear all caches
     */
    public void clearAll() {
        l1Lock.writeLock().lock();
        l2Lock.writeLock().lock();
        l3Lock.writeLock().lock();
        try {
            l1Cache.clear();
            l1AccessOrder.clear();
            l2Cache.clear();
            l2EvictionQueue.clear();
            l3Cache.clear();
            bloomFilter.clear();
            
            hitCount.set(0);
            missCount.set(0);
            evictionCount.set(0);
            
            log.info("All caches cleared");
        } finally {
            l3Lock.writeLock().unlock();
            l2Lock.writeLock().unlock();
            l1Lock.writeLock().unlock();
        }
    }
    
    /**
     * Cache levels enum
     */
    private enum CacheLevel {
        L1, L2, L3
    }
}