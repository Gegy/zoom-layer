package net.gegy1000.zoomlayer;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.Arrays;

/**
 * A simple, concurrent, fixed-size cache for storing the results produced by biome layers.
 * <p>
 * It is not important within this context for a cache to have guarantees on how long entries stay cached. It is just
 * useful in the case that an entry is cached that it can be used instead of needing to recompute the value.
 * <p>
 * This allows this implementation to be highly optimized and thread-safe while also being lock-free: there is no
 * consequence to values overriding each other, so that does not need to be protected against.
 * <p>
 * Furthermore, entries do not need to be removed: through entries overriding each other, the size does not need to be
 * maintained by evicting old entries.
 * <p>
 * Note on key and value sizes:
 * The X and Z components of the keys are limited to 24 bits, and the value is limited to 16 bits. This is such that
 * both together can be packed into a single 64-bit long.
 * <p>
 * Due to Java not having support for value types, without allocating Objects on the heap, it is not possible to store
 * values larger than a long. To store the coordinate key and value at full precision is not be possible within a long.
 * <p>
 * In the case of this cache where it needs to support concurrent access, it is also not possible to split
 * these over a mutable object or multiple arrays due to the possibility for introducing race conditions.
 */
public final class ConcurrentLayerCache {
    private static final long KEY_COMP_WIDTH = 24;

    private static final long KEY_COMP_MASK = (1 << KEY_COMP_WIDTH) - 1;
    private static final long KEY_MASK = (KEY_COMP_MASK << KEY_COMP_WIDTH) | KEY_COMP_MASK;

    private static final long VALUE_WIDTH = 16;
    private static final long VALUE_MASK = (1 << VALUE_WIDTH) - 1;

    private final long[] entries;

    private final int capacity;
    private final int mask;

    public ConcurrentLayerCache(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.entries = new long[this.capacity];
        Arrays.fill(this.entries, Long.MIN_VALUE);
    }

    /**
     * Retrieves a value from the cache at the given key, or computes and stores the value if it is not already present.
     *
     * @param x x key
     * @param z z key
     * @param operator operator to compute the value if it is not already cached
     * @return the retrieved or newly computed value
     */
    public int get(int x, int z, LayerOperator operator) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        // if the entry here has a key that matches ours, we have a cache hit
        long entry = this.entries[idx];
        if (unpackKey(entry) == key) {
            return unpackValue(entry);
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = operator.apply(x, z);
        this.entries[idx] = pack(key, sampled);

        return sampled;
    }

    public int capacity() {
        return this.capacity;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return (x & KEY_COMP_MASK) << KEY_COMP_WIDTH | z & KEY_COMP_MASK;
    }

    private static long pack(long key, int value) {
        return (key & KEY_MASK) << VALUE_WIDTH | value & VALUE_MASK;
    }

    private static long unpackKey(long packed) {
        return packed >> VALUE_WIDTH & KEY_MASK;
    }

    private static int unpackValue(long packed) {
        return (int) (packed & VALUE_MASK);
    }
}
