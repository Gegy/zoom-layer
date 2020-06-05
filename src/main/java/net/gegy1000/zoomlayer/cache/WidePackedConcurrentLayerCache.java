package net.gegy1000.zoomlayer.cache;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.Arrays;

public final class WidePackedConcurrentLayerCache implements BiomeLayerCache {
    private static final long KEY_COMP_WIDTH = 24;

    private static final long KEY_COMP_MASK = (1 << KEY_COMP_WIDTH) - 1;
    private static final long KEY_MASK = (KEY_COMP_MASK << KEY_COMP_WIDTH) | KEY_COMP_MASK;

    private static final long VALUE_WIDTH = 16;
    private static final long VALUE_MASK = (1 << VALUE_WIDTH) - 1;

    private final long[] entriesHigh;
    private final long[] entriesLow;

    private final int capacity;
    private final int mask;

    public WidePackedConcurrentLayerCache(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.entriesHigh = new long[this.capacity];
        Arrays.fill(this.entriesHigh, Long.MIN_VALUE);
        this.entriesLow = new long[this.capacity];
        Arrays.fill(this.entriesLow, Long.MIN_VALUE);
    }

    @Override
    public int get(int x, int z, LayerOperator operator) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        // if the entry here has a key that matches ours, we have a cache hit
        long entryHigh = this.entriesHigh[idx];
        long entryLow = this.entriesLow[idx];
        if (unpackKey(entryHigh) == key && unpackKey(entryLow) == key) {
            return unpackValue(entryHigh) << VALUE_WIDTH | unpackValue(entryLow);
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = operator.apply(x, z);

        this.entriesHigh[idx] = pack(key, sampled >> VALUE_WIDTH);
        this.entriesLow[idx] = pack(key, (int) (sampled & VALUE_MASK));

        return sampled;
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
