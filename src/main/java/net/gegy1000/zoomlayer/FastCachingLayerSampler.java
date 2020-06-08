package net.gegy1000.zoomlayer;

import java.util.Arrays;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;

public final class FastCachingLayerSampler extends CachingLayerSampler {
    private final long[] keys;
    private final int[] values;

    private final int capacity;
    private final int mask;

    public FastCachingLayerSampler(int capacity, LayerOperator operator) {
        super(new Long2IntLinkedOpenHashMap(0), 0, operator);

        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.keys = new long[this.capacity];
        Arrays.fill(this.keys, Long.MIN_VALUE);
        this.values = new int[this.capacity];
    }

    @Override
    public int sample(int x, int z) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        // if the entry here has a key that matches ours, we have a cache hit
        if (this.keys[idx] == key) {
            return this.values[idx];
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = this.operator.apply(x, z);
        this.keys[idx] = key;
        this.values[idx] = sampled;

        return sampled;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return ChunkPos.toLong(x, z);
    }
}
