package net.gegy1000.zoomlayer.mixin;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeLayerSampler.class)
public abstract class MixinBiomeLayerSampler {
    private ThreadLocal<CachingLayerSampler> tlSampler;
    private Thread lastThread;
    private CachingLayerSampler lastSampler;

    @Shadow
    protected abstract Biome getBiome(int id);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(LayerFactory<CachingLayerSampler> factory, CallbackInfo ci) {
        this.tlSampler = ThreadLocal.withInitial(factory::make);
    }

    /**
     * Replace with implementation that accesses from ThreadLocal
     *
     * @author gegy1000
     */
    @Overwrite
    public Biome sample(int x, int y) {
        Thread thread = Thread.currentThread();
        if (thread == this.lastThread) {
            return this.getBiome(this.lastSampler.sample(x, y));
        }

        CachingLayerSampler tlSampler = this.tlSampler.get();
        this.lastThread = thread;
        this.lastSampler = tlSampler;

        return this.getBiome(tlSampler.sample(x, y));
    }
}
