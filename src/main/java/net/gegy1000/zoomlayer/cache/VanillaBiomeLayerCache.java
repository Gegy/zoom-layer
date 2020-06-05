package net.gegy1000.zoomlayer.cache;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.layer.util.LayerOperator;

public final class VanillaBiomeLayerCache implements BiomeLayerCache {
    private final Long2IntLinkedOpenHashMap cache;
    private final int capacity;

    public VanillaBiomeLayerCache(int capacity) {
        this.cache = new Long2IntLinkedOpenHashMap(capacity / 4);
        this.cache.defaultReturnValue(Integer.MIN_VALUE);
        this.capacity = capacity;
    }

    @Override
    public int get(int x, int z, LayerOperator operator) {
        long key = ChunkPos.toLong(x, z);
        synchronized (this.cache) {
            int cached = this.cache.get(key);
            if (cached != Integer.MIN_VALUE) {
                return cached;
            }

            int sampled = operator.apply(x, z);
            this.cache.put(key, sampled);
            if (this.cache.size() > this.capacity) {
                for (int i = 0; i < this.capacity / 16; ++i) {
                    this.cache.removeFirstInt();
                }
            }

            return sampled;
        }
    }
}
