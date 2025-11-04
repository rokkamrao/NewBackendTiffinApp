package com.tiffin.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Entry with advanced metadata tracking
 * Similar to cache entries used in high-performance systems like Gmail/Instagram
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheEntry {
    
    private Object value;
    private long expirationTime;
    private long creationTime;
    private long lastAccessTime;
    private AtomicLong accessCount;
    private double frequency; // For LFU calculations
    private int priority; // For priority-based eviction
    
    public CacheEntry(Object value, long expirationTime) {
        this.value = value;
        this.expirationTime = expirationTime;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = this.creationTime;
        this.accessCount = new AtomicLong(0);
        this.frequency = 0.0;
        this.priority = 1;
    }
    
    /**
     * Check if entry has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * Update last access time
     */
    public void updateAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
        updateFrequency();
    }
    
    /**
     * Increment access count atomically
     */
    public long incrementAccessCount() {
        return accessCount.incrementAndGet();
    }
    
    /**
     * Get current access count
     */
    public long getAccessCount() {
        return accessCount.get();
    }
    
    /**
     * Update frequency for LFU calculations
     * Uses exponential decay to give more weight to recent accesses
     */
    private void updateFrequency() {
        long timeDiff = System.currentTimeMillis() - lastAccessTime;
        double decay = Math.exp(-timeDiff / 300000.0); // 5-minute decay
        this.frequency = (frequency * decay) + 1.0;
    }
    
    /**
     * Calculate entry score for eviction decisions
     * Combines frequency, recency, and priority
     */
    public double getEvictionScore() {
        long age = System.currentTimeMillis() - lastAccessTime;
        double recency = 1.0 / (1.0 + age / 60000.0); // Recency in minutes
        
        return (frequency * 0.4) + (recency * 0.4) + (priority * 0.2);
    }
    
    /**
     * Check if entry is hot (frequently accessed recently)
     */
    public boolean isHot() {
        return frequency > 10.0 && (System.currentTimeMillis() - lastAccessTime) < 300000; // 5 minutes
    }
    
    /**
     * Check if entry is warm (moderately accessed)
     */
    public boolean isWarm() {
        return frequency > 2.0 && (System.currentTimeMillis() - lastAccessTime) < 1800000; // 30 minutes
    }
    
    /**
     * Check if entry is cold (rarely accessed)
     */
    public boolean isCold() {
        return !isHot() && !isWarm();
    }
}