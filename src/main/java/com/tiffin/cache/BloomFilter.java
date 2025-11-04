package com.tiffin.cache;

import java.util.BitSet;
import java.util.function.Function;
import java.util.List;
import java.util.Arrays;

/**
 * Bloom Filter implementation for fast negative lookups
 * Used to quickly determine if a key is definitely not in cache
 * Reduces unnecessary cache lookups significantly
 */
public class BloomFilter {
    
    private final BitSet bitSet;
    private final int size;
    private final int hashFunctions;
    private final List<Function<String, Integer>> hashers;
    
    public BloomFilter(int expectedElements, double falsePositiveRate) {
        // Calculate optimal size and hash functions
        this.size = calculateOptimalSize(expectedElements, falsePositiveRate);
        this.hashFunctions = calculateOptimalHashFunctions(expectedElements, size);
        this.bitSet = new BitSet(size);
        
        // Initialize hash functions
        this.hashers = Arrays.asList(
            key -> Math.abs(key.hashCode() % size),
            key -> Math.abs(key.hashCode() * 31 % size),
            key -> Math.abs(murmurHash3(key) % size),
            key -> Math.abs(djb2Hash(key) % size),
            key -> Math.abs(sdbmHash(key) % size)
        );
    }
    
    /**
     * Add key to bloom filter
     */
    public void add(String key) {
        for (int i = 0; i < hashFunctions && i < hashers.size(); i++) {
            int hash = hashers.get(i).apply(key);
            bitSet.set(hash);
        }
    }
    
    /**
     * Check if key might be in the set
     * Returns false if definitely not present
     * Returns true if might be present (with some false positive rate)
     */
    public boolean mightContain(String key) {
        for (int i = 0; i < hashFunctions && i < hashers.size(); i++) {
            int hash = hashers.get(i).apply(key);
            if (!bitSet.get(hash)) {
                return false; // Definitely not present
            }
        }
        return true; // Might be present
    }
    
    /**
     * Clear the bloom filter
     */
    public void clear() {
        bitSet.clear();
    }
    
    /**
     * Get the current false positive rate
     */
    public double getCurrentFalsePositiveRate() {
        int setBits = bitSet.cardinality();
        double ratio = (double) setBits / size;
        return Math.pow(ratio, hashFunctions);
    }
    
    /**
     * Calculate optimal bit array size
     */
    private int calculateOptimalSize(int expectedElements, double falsePositiveRate) {
        return (int) Math.ceil(-expectedElements * Math.log(falsePositiveRate) / (Math.log(2) * Math.log(2)));
    }
    
    /**
     * Calculate optimal number of hash functions
     */
    private int calculateOptimalHashFunctions(int expectedElements, int size) {
        return Math.max(1, (int) Math.round((double) size / expectedElements * Math.log(2)));
    }
    
    /**
     * MurmurHash3 implementation for better hash distribution
     */
    private int murmurHash3(String key) {
        byte[] data = key.getBytes();
        int length = data.length;
        int seed = 0;
        
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;
        final int r1 = 15;
        final int r2 = 13;
        final int m = 5;
        final int n = 0xe6546b64;
        
        int hash = seed;
        
        final int nblocks = length / 4;
        
        // Process 4-byte blocks
        for (int i = 0; i < nblocks; i++) {
            int k = (data[i * 4] & 0xff) |
                   ((data[i * 4 + 1] & 0xff) << 8) |
                   ((data[i * 4 + 2] & 0xff) << 16) |
                   ((data[i * 4 + 3] & 0xff) << 24);
            
            k *= c1;
            k = Integer.rotateLeft(k, r1);
            k *= c2;
            
            hash ^= k;
            hash = Integer.rotateLeft(hash, r2);
            hash = hash * m + n;
        }
        
        // Process remaining bytes
        int k1 = 0;
        int remaining = length & 3;
        
        if (remaining >= 3) k1 ^= (data[nblocks * 4 + 2] & 0xff) << 16;
        if (remaining >= 2) k1 ^= (data[nblocks * 4 + 1] & 0xff) << 8;
        if (remaining >= 1) {
            k1 ^= (data[nblocks * 4] & 0xff);
            k1 *= c1;
            k1 = Integer.rotateLeft(k1, r1);
            k1 *= c2;
            hash ^= k1;
        }
        
        // Finalization
        hash ^= length;
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);
        
        return hash;
    }
    
    /**
     * DJB2 hash function
     */
    private int djb2Hash(String key) {
        int hash = 5381;
        for (char c : key.toCharArray()) {
            hash = ((hash << 5) + hash) + c;
        }
        return hash;
    }
    
    /**
     * SDBM hash function
     */
    private int sdbmHash(String key) {
        int hash = 0;
        for (char c : key.toCharArray()) {
            hash = c + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
}