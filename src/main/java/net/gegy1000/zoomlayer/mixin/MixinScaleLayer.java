package net.gegy1000.zoomlayer.mixin;

import net.minecraft.world.biome.layer.ScaleLayer;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ScaleLayer.class)
public abstract class MixinScaleLayer {
    @Shadow
    public abstract int transformX(int x);

    @Shadow
    public abstract int transformZ(int y);

    @Shadow
    protected abstract int sample(LayerSampleContext<?> ctx, int tl, int tr, int bl, int br);

    @Overwrite
    public int sample(LayerSampleContext<?> ctx, LayerSampler parent, int x, int z) {
        int tl = parent.sample(this.transformX(x), this.transformZ(z));
        int ix = x & 1;
        int iz = z & 1;

        if (ix == 0 && iz == 0) return tl;

        ctx.initSeed(x & ~1, z & ~1);

        if (ix == 0) {
            int bl = parent.sample(this.transformX(x), this.transformZ(z + 1));
            return ctx.choose(tl, bl);
        }

        // move `choose` into above if-statement: maintain rng parity
        ctx.choose(0, 0);

        if (iz == 0) {
            int tr = parent.sample(this.transformX(x + 1), this.transformZ(z));
            return ctx.choose(tl, tr);
        }

        // move `choose` into above if-statement: maintain rng parity
        ctx.choose(0, 0);

        int bl = parent.sample(this.transformX(x), this.transformZ(z + 1));
        int tr = parent.sample(this.transformX(x + 1), this.transformZ(z));
        int br = parent.sample(this.transformX(x + 1), this.transformZ(z + 1));

        return this.sample(ctx, tl, tr, bl, br);
    }
}
