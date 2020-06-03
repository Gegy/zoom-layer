package net.gegy1000.zoomlayer.mixin;

import net.gegy1000.zoomlayer.ConcurrentLayerCache;
import net.gegy1000.zoomlayer.FastCachingLayerSampler;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;
import net.minecraft.world.biome.source.SeedMixer;

import net.gegy1000.zoomlayer.CachingLayerAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CachingLayerContext.class)
public class MixinCachingLayerContext implements CachingLayerAccess {
    @Shadow private long localSeed;
    @Shadow @Final private long worldSeed;
    private ConcurrentLayerCache fastCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(int capacity, long seed, long salt, CallbackInfo ci) {
        this.fastCache = new ConcurrentLayerCache(512);
    }

    /**
     * Replace with optimized implementation
     *
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator) {
        return new FastCachingLayerSampler(this.fastCache, operator);
    }

    /**
     * Replace with optimized implementation
     *
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler sampler) {
        return new FastCachingLayerSampler(this.fastCache, operator);
    }

    /**
     * Replace with optimized implementation
     *
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler left, CachingLayerSampler right) {
        return new FastCachingLayerSampler(this.fastCache, operator);
    }

    @Override
    public void skipInt() {
        this.localSeed = SeedMixer.mixSeed(this.localSeed, this.worldSeed);
    }
}
