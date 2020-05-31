package net.gegy1000.zoomlayer;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;

public final class FastCachingLayerSampler extends CachingLayerSampler {
    private final ConcurrentLayerCache fastCache;

    public FastCachingLayerSampler(ConcurrentLayerCache cache, LayerOperator operator) {
        super(new Long2IntLinkedOpenHashMap(0), 0, operator);
        this.fastCache = cache;
    }

    @Override
    public int sample(int x, int z) {
        return this.fastCache.get(x, z, this.operator);
    }
}
