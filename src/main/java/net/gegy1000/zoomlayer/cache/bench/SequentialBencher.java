package net.gegy1000.zoomlayer.cache.bench;

import net.gegy1000.zoomlayer.cache.BiomeLayerCache;

import java.util.function.Supplier;

public final class SequentialBencher implements Bencher {
    @Override
    public long bench(Supplier<BiomeLayerCache> cacheSupplier) {
        BiomeLayerCache cache = cacheSupplier.get();

        long start = System.currentTimeMillis();
        for (int x = 0; x < 1000000; x++) {
            cache.get(x, 0, (x1, z) -> 0);
        }

        return System.currentTimeMillis() - start;
    }
}
