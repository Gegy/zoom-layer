package net.gegy1000.zoomlayer.cache.bench;

import net.gegy1000.zoomlayer.cache.BiomeLayerCache;

import java.util.function.Supplier;

public interface Bencher {
    long bench(Supplier<BiomeLayerCache> cacheSupplier);
}
