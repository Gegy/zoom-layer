package net.gegy1000.zoomlayer.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.gegy1000.zoomlayer.BenchCommand;
import net.minecraft.server.command.LocateBiomeCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocateBiomeCommand.class)
public class MixinLocateBiomeCommand {
    @Inject(method = "register", at = @At("RETURN"))
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        BenchCommand.register(dispatcher);
    }
}
