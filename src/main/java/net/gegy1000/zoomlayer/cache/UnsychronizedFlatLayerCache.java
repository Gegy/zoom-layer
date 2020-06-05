package net.gegy1000.zoomlayer.cache;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.Arrays;

public final class UnsychronizedFlatLayerCache implements BiomeLayerCache {
    private final long[] entries;

    private final int capacity;
    private final int mask;

    public UnsychronizedFlatLayerCache(int capacity) {
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
        if (this.entries[idx] == key) {
            return (int) this.entries[idx + 1];
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = operator.apply(x, z);
        this.entries[idx] = key;
        this.entries[idx + 1] = sampled;

        return sampled;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return ChunkPos.toLong(x, z);
    }
}
