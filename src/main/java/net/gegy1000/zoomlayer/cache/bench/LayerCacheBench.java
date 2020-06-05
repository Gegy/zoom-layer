package net.gegy1000.zoomlayer.cache.bench;

import net.gegy1000.zoomlayer.cache.BiomeLayerCache;
import net.gegy1000.zoomlayer.cache.NLockLayerCache;
import net.gegy1000.zoomlayer.cache.OneLockLayerCache;
import net.gegy1000.zoomlayer.cache.OneSpinLockLayerCache;
import net.gegy1000.zoomlayer.cache.PackedConcurrentLayerCache;
import net.gegy1000.zoomlayer.cache.UnsychronizedFlatLayerCache;
import net.gegy1000.zoomlayer.cache.UnsychronizedLayerCache;
import net.gegy1000.zoomlayer.cache.VanillaBiomeLayerCache;
import net.gegy1000.zoomlayer.cache.WidePackedConcurrentLayerCache;
import net.gegy1000.zoomlayer.cache.WidePackedFlatConcurrentLayerCache;

import java.util.function.Supplier;

public final class LayerCacheBench {
    private static final int CAPACITY = 25 * 4;

    public static void main(String[] args) {
        System.out.println("benching sequential:");
        batchBenchAllCaches(new SequentialBencher());
        System.out.println();

        System.out.println("benching layered:");
        batchBenchAllCaches(new LayeredBencher());
        System.out.println();
    }

    private static void batchBenchAllCaches(Bencher bencher) {
        System.out.println("  benching vanilla:");
        Results vanilla = batchBench(LayerCacheBench::vanilla, bencher);
        printResults(vanilla);

        System.out.println("  benching packed concurrent:");
        Results packedConcurrent = batchBench(LayerCacheBench::packedConcurrent, bencher);
        printResults(packedConcurrent);

        System.out.println("  benching wide packed concurrent:");
        Results widePackedConcurrent = batchBench(LayerCacheBench::widePackedConcurrent, bencher);
        printResults(widePackedConcurrent);

        System.out.println("  benching wide packed flat concurrent:");
        Results widePackedFlatConcurrent = batchBench(LayerCacheBench::widePackedFlatConcurrent, bencher);
        printResults(widePackedFlatConcurrent);

        System.out.println("  benching one lock:");
        Results oneLock = batchBench(LayerCacheBench::oneLock, bencher);
        printResults(oneLock);

        System.out.println("  benching n lock:");
        Results nLock = batchBench(LayerCacheBench::nLock, bencher);
        printResults(nLock);

        System.out.println("  benching one spin lock:");
        Results oneSpinLock = batchBench(LayerCacheBench::oneSpinLock, bencher);
        printResults(oneSpinLock);

        System.out.println("  benching unsynchronized unpacked:");
        Results singleThreadUnpacked = batchBench(LayerCacheBench::unsynchronizedUnpacked, bencher);
        printResults(singleThreadUnpacked);

        System.out.println("  benching unsynchronized unpacked flat:");
        Results singleThreadUnpackedFlat = batchBench(LayerCacheBench::unsynchronizedUnpackedFlat, bencher);
        printResults(singleThreadUnpackedFlat);
    }

    private static void printResults(Results results) {
        System.out.println("    min: " + results.min + "ms");
        System.out.println("    max: " + results.max + "ms");
        System.out.println("    mean: " + results.mean + "ms");
    }

    private static Results batchBench(Supplier<BiomeLayerCache> cacheSupplier, Bencher bencher) {
        // warm up
        for (int i = 0; i < 10; i++) {
            bencher.bench(cacheSupplier);
        }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long total = 0;
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            long time = bencher.bench(cacheSupplier);
            min = Math.min(time, min);
            max = Math.max(time, max);
            total += time;
        }

        return new Results(min, max, total / iterations);
    }

    private static BiomeLayerCache vanilla() {
        return new VanillaBiomeLayerCache(CAPACITY);
    }

    private static BiomeLayerCache packedConcurrent() {
        return new PackedConcurrentLayerCache(CAPACITY);
    }

    private static BiomeLayerCache widePackedConcurrent() {
        return new WidePackedConcurrentLayerCache(CAPACITY);
    }

    private static BiomeLayerCache widePackedFlatConcurrent() {
        return new WidePackedFlatConcurrentLayerCache(CAPACITY);
    }

    private static BiomeLayerCache oneLock() {
        return new OneLockLayerCache(CAPACITY);
    }

    private static BiomeLayerCache nLock() {
        return new NLockLayerCache(CAPACITY);
    }

    private static BiomeLayerCache oneSpinLock() {
        return new OneSpinLockLayerCache(CAPACITY);
    }

    private static BiomeLayerCache unsynchronizedUnpacked() {
        return new UnsychronizedLayerCache(CAPACITY);
    }

    private static BiomeLayerCache unsynchronizedUnpackedFlat() {
        return new UnsychronizedFlatLayerCache(CAPACITY);
    }

    static class Results {
        final long min;
        final long max;
        final long mean;

        Results(long min, long max, long mean) {
            this.min = min;
            this.max = max;
            this.mean = mean;
        }
    }
}
