package net.gegy1000.zoomlayer.cache.bench;

import net.gegy1000.zoomlayer.cache.BiomeLayerCache;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.function.Supplier;

public final class LayeredBencher implements Bencher {
    @Override
    public long bench(Supplier<BiomeLayerCache> cacheSupplier) {
        LayerSampler sampler = buildLayers(() -> new LayerContext(cacheSupplier.get())).create();

        long start = System.currentTimeMillis();
        for (int z = -256; z < 256; z++) {
            for (int x = -256; x < 256; x++) {
                sampler.sample(x, z);
            }
        }

        return System.currentTimeMillis() - start;
    }

    static LayerFactory buildLayers(Supplier<LayerContext> context) {
        LayerFactory continents = initContinent(context.get());
        continents = scaleFuzzy(context.get(), continents);
        continents = scaleFuzzy(context.get(), continents);

        LayerFactory withIslands = addIsland(context.get(), continents);
        withIslands = scaleFuzzy(context.get(), withIslands);
        withIslands = scaleFuzzy(context.get(), withIslands);

        LayerFactory seededLand = seedLand(context.get(), withIslands);
        seededLand = scaleFuzzy(context.get(), seededLand);

        return seededLand;
    }

    static LayerFactory initContinent(LayerContext context) {
        return () -> context.createSampler((x, z) -> {
            int seed = x * 31431 * z * 43161;
            return seed & 1;
        });
    }

    static LayerFactory scaleFuzzy(LayerContext context, LayerFactory parent) {
        return () -> {
            LayerSampler parentSampler = parent.create();
            return context.createSampler((x, z) -> {
                int px = x >> 1;
                int pz = z >> 1;

                int tl = parentSampler.sample(px, pz);
                int tr = parentSampler.sample(px + 1, pz);
                int bl = parentSampler.sample(px, pz + 1);
                int br = parentSampler.sample(px + 1, pz + 1);

                int seed = x * 2961 * z * 23651;
                switch (seed & 3) {
                    case 0: return tl;
                    case 1: return tr;
                    case 2: return bl;
                    default: return br;
                }
            });
        };
    }

    static LayerFactory addIsland(LayerContext context, LayerFactory parent) {
        return () -> {
            LayerSampler parentSampler = parent.create();
            return context.createSampler((x, z) -> {
                int o = parentSampler.sample(x, z);
                int n = parentSampler.sample(x, z - 1);
                int e = parentSampler.sample(x + 1, z);
                int s = parentSampler.sample(x, z + 1);
                int w = parentSampler.sample(x - 1, z);
                if (o == 0 && n == 0 && e == 0 && s == 0 && w == 0) {
                    return 1;
                }
                return o;
            });
        };
    }

    static LayerFactory seedLand(LayerContext context, LayerFactory parent) {
        return () -> {
            LayerSampler parentSampler = parent.create();
            return context.createSampler((x, z) -> {
                int sample = parentSampler.sample(x, z);
                if (sample == 1) {
                    int seed = x * 947123 * z * 43151;
                    return (seed & 3) + 1;
                }
                return 0;
            });
        };
    }

    static class LayerSampler {
        final LayerOperator operator;
        final BiomeLayerCache cache;

        LayerSampler(LayerOperator operator, BiomeLayerCache cache) {
            this.operator = operator;
            this.cache = cache;
        }

        int sample(int x, int z) {
            return this.cache.get(x, z, this.operator);
        }
    }

    static class LayerContext {
        final BiomeLayerCache cache;

        LayerContext(BiomeLayerCache cache) {
            this.cache = cache;
        }

        LayerSampler createSampler(LayerOperator operator) {
            return new LayerSampler(operator, this.cache);
        }
    }

    interface LayerFactory {
        LayerSampler create();
    }
}
