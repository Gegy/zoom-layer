package net.gegy1000.zoomlayer.cache.bench;

import net.gegy1000.zoomlayer.cache.BiomeLayerCache;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.function.Supplier;

public final class LayeredBencher implements Bencher {
    @Override
    public long bench(Supplier<BiomeLayerCache> cacheSupplier) {
        LayerSampler sampler = buildLayers(() -> new LayerContext(cacheSupplier.get())).create();

        long start = System.currentTimeMillis();
        for (int z = -512; z < 512; z++) {
            for (int x = -512; x < 512; x++) {
                sampler.sample(x, z);
            }
        }

        return System.currentTimeMillis() - start;
    }

    static LayerFactory buildLayers(Supplier<LayerContext> context) {
        LayerFactory continents = initContinent(context.get());
        continents = scaleNn(context.get(), continents);
        continents = scaleNn(context.get(), continents);

        LayerFactory withIslands = addIsland(context.get(), continents);
        withIslands = scaleNn(context.get(), withIslands);
        withIslands = scaleNn(context.get(), withIslands);

        return seedLand(context.get(), withIslands);
    }

    static LayerFactory initContinent(LayerContext context) {
        return () -> context.createSampler((x, z) -> {
            int seed = x * 31431 * z * 43161;
            return seed & 1;
        });
    }

    static LayerFactory scaleNn(LayerContext context, LayerFactory parent) {
        return () -> {
            LayerSampler parentSampler = parent.create();
            return context.createSampler((x, z) -> {
                return parentSampler.sample(x >> 1, z >> 1);
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
                    return (seed & 4) + 1;
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
