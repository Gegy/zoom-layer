package net.gegy1000.zoomlayer.mixin;

import net.gegy1000.zoomlayer.CachingLayerAccess;
import net.gegy1000.zoomlayer.FastCachingLayerSampler;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;
import net.minecraft.world.biome.source.SeedMixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CachingLayerContext.class)
public class MixinCachingLayerContext implements CachingLayerAccess {
    @Shadow
    private long localSeed;
    @Shadow
    @Final
    private long worldSeed;

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

    @Override
    public void skipInt() {
        this.localSeed = SeedMixer.mixSeed(this.localSeed, this.worldSeed);
    }
}
