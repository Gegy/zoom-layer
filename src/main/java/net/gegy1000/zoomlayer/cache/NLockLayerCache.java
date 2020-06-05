package net.gegy1000.zoomlayer.cache;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.LayerOperator;

public final class NLockLayerCache implements BiomeLayerCache {
    private final Entry[] entries;

    private final int capacity;
    private final int mask;

    public NLockLayerCache(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.entries = new Entry[this.capacity];
        for (int i = 0; i < this.capacity; i++) {
            this.entries[i] = new Entry();
        }
    }

    @Override
    public int get(int x, int z, LayerOperator operator) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        Entry entry = this.entries[idx];

        // if the entry here has a key that matches ours, we have a cache hit
        synchronized (entry) {
            if (entry.key == key) {
                return entry.value;
            }
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = operator.apply(x, z);
        synchronized (entry) {
            entry.key = key;
            entry.value = sampled;
        }

        return sampled;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return ChunkPos.toLong(x, z);
    }

    static class Entry {
        long key = Long.MIN_VALUE;
        int value;
    }
}
