package net.gegy1000.zoomlayer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.world.biome.source.BiomeSource;

import static net.minecraft.server.command.CommandManager.literal;

public final class BenchCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("bench")
                .then(literal("layers")
                    .executes(BenchCommand::benchLayers)
                )
        );
        // @formatter:on
    }

    private static int benchLayers(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BiomeSource biomeSource = world.getChunkManager().getChunkGenerator().getBiomeSource();

        // some coordinates that are not hopefully loaded by the player
        int x = -123456;
        int z = 654321;
        int rad = 500;

        long start = System.currentTimeMillis();
        for (int dz = -rad; dz < rad; dz++) {
            for (int dx = -rad; dx < rad; dx++) {
                biomeSource.getBiomeForNoiseGen(x + dx, 0, z + dz);
            }
        }

        long time = System.currentTimeMillis() - start;

        source.sendFeedback(new LiteralText("Benchmark took " + time + "ms"), false);

        return Command.SINGLE_SUCCESS;
    }
}
