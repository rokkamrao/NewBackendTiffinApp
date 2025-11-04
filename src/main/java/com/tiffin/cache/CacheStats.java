package com.tiffin.cache;

import lombok.Builder;
import lombok.Data;

/**
 * Cache statistics for monitoring and optimization
 */
@Data
@Builder
public class CacheStats {
    private int l1Size;
    private int l2Size;
    private int l3Size;
    private long hitCount;
    private long missCount;
    private long evictionCount;
    private double hitRatio;
    
    public int getTotalSize() {
        return l1Size + l2Size + l3Size;
    }
    
    public long getTotalRequests() {
        return hitCount + missCount;
    }
}