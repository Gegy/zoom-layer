package net.gegy1000.zoomlayer.cache;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class OneSpinLockLayerCache implements BiomeLayerCache {
    private static final int FREE = 0;
    private static final int LOCKED = 1;

    private final long[] keys;
    private final int[] values;

    private final int capacity;
    private final int mask;

    private final AtomicInteger lock = new AtomicInteger(FREE);

    public OneSpinLockLayerCache(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.keys = new long[this.capacity];
        Arrays.fill(this.keys, Long.MIN_VALUE);
        this.values = new int[this.capacity];
    }

    @Override
    public int get(int x, int z, LayerOperator operator) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        // spin until lock is READ
        while (!this.lock.compareAndSet(FREE, FREE)) {
            // spin
        }

        // if the entry here has a key that matches ours, we have a cache hit
        if (this.keys[idx] == key) {
            return this.values[idx];
        }

        // spin until we can acquire lock
        while (!this.lock.compareAndSet(FREE, LOCKED)) {
            // spin
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = operator.apply(x, z);
        this.keys[idx] = key;
        this.values[idx] = sampled;

        // release lock
        this.lock.set(FREE);

        return sampled;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return ChunkPos.toLong(x, z);
    }
}
