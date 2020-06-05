package net.gegy1000.zoomlayer.cache;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.Arrays;

public final class WidePackedFlatConcurrentLayerCache implements BiomeLayerCache {
    private static final long KEY_COMP_WIDTH = 24;

    private static final long KEY_COMP_MASK = (1 << KEY_COMP_WIDTH) - 1;
    private static final long KEY_MASK = (KEY_COMP_MASK << KEY_COMP_WIDTH) | KEY_COMP_MASK;

    private static final long VALUE_WIDTH = 16;
    private static final long VALUE_MASK = (1 << VALUE_WIDTH) - 1;

    private final long[] entries;

    private final int capacity;
    private final int mask;

    public WidePackedFlatConcurrentLayerCache(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.entries = new long[this.capacity * 2];
        Arrays.fill(this.entries, Long.MIN_VALUE);
    }

    @Override
    public int get(int x, int z, LayerOperator operator) {
        long key = key(x, z);
        int idx = (hash(key) & this.mask) << 1;

        // if the entry here has a key that matches ours, we have a cache hit
        long entryHigh = this.entries[idx];
        long entryLow = this.entries[idx + 1];
        if (unpackKey(entryHigh) == key && unpackKey(entryLow) == key) {
            return unpackValue(entryHigh) << VALUE_WIDTH | unpackValue(entryLow);
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = operator.apply(x, z);

        this.entries[idx] = pack(key, sampled >> VALUE_WIDTH);
        this.entries[idx + 1] = pack(key, (int) (sampled & VALUE_MASK));

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
