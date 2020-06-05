package net.gegy1000.zoomlayer.cache;

import net.minecraft.world.biome.layer.util.LayerOperator;

//TODO: store operator in cache?
public interface BiomeLayerCache {
    int get(int x, int z, LayerOperator operator);
}
