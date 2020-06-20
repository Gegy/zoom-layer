package net.gegy1000.zoomlayer.mixin;

import net.gegy1000.zoomlayer.FastCachingLayerSampler;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CachingLayerContext.class)
public class MixinCachingLayerContext {
    /**
     * Replace with optimized implementation
     *
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator) {
        return new FastCachingLayerSampler(128, operator);
    }

    /**
     * Replace with optimized implementation
     *
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler sampler) {
        return new FastCachingLayerSampler(128, operator);
    }

    /**
     * Replace with optimized implementation
     *
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler left, CachingLayerSampler right) {
        return new FastCachingLayerSampler(128, operator);
    }
}
